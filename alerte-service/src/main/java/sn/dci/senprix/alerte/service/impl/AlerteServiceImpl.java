package sn.dci.senprix.alerte.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.alerte.client.NotifServiceClient;
import sn.dci.senprix.alerte.config.SeuilAlertesProperties;
import sn.dci.senprix.alerte.dto.AlerteCreationRequest;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.dto.ResolutionRequest;
import sn.dci.senprix.alerte.entity.Alerte;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;
import sn.dci.senprix.alerte.exception.AlerteNotFoundException;
import sn.dci.senprix.alerte.exception.TransitionInvalideException;
import sn.dci.senprix.alerte.mapper.AlerteMapper;
import sn.dci.senprix.alerte.repository.AlerteRepository;
import sn.dci.senprix.alerte.service.AlerteService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service métier de gestion des alertes.
 * Calcule l'écart en pourcentage entre le montant suspect et la
 * moyenne du marché, puis déduit le niveau de gravité correspondant :
 * FAIBLE (50-100%), MOYENNE (100-200%), ELEVEE (plus de 200%).
 * Notifie également le notif-service à chaque création, pour qu'un
 * email d'alerte soit envoyé aux administrateurs.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlerteServiceImpl implements AlerteService {

    private static final BigDecimal CENT = new BigDecimal("100");

    private final AlerteRepository alerteRepository;
    private final AlerteMapper alerteMapper;
    private final NotifServiceClient notifServiceClient;
    private final SeuilAlertesProperties seuilProperties;

    @Override
    public AlerteResponse creer(AlerteCreationRequest request) {
        BigDecimal ecartPourcentage = calculerEcartPourcentage(
                request.getMontant(), request.getMontantMoyen());
        NiveauGravite gravite = determinerGravite(ecartPourcentage);

        Alerte alerte = Alerte.builder()
                .prixId(request.getPrixId())
                .produitId(request.getProduitId())
                .marcheId(request.getMarcheId())
                .montant(request.getMontant())
                .montantMoyen(request.getMontantMoyen())
                .ecartPourcentage(ecartPourcentage)
                .niveauGravite(gravite)
                .statut(StatutAlerte.NOUVELLE)
                .build();

        alerte = alerteRepository.save(alerte);
        notifServiceClient.notifierNouvelleAlerte(alerte);

        return alerteMapper.toResponse(alerte);
    }

    @Override
    @Transactional(readOnly = true)
    public AlerteResponse obtenirParId(Long id) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new AlerteNotFoundException(id));
        return alerteMapper.toResponse(alerte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteResponse> listerToutes() {
        return alerteRepository.findAll().stream().map(alerteMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteResponse> listerParStatut(String statut) {
        StatutAlerte statutEnum = StatutAlerte.valueOf(statut.toUpperCase());
        return alerteRepository.findByStatut(statutEnum)
                .stream().map(alerteMapper::toResponse).toList();
    }

    @Override
    public AlerteResponse prendreEnCharge(Long id) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new AlerteNotFoundException(id));

        if (alerte.getStatut() != StatutAlerte.NOUVELLE) {
            throw new TransitionInvalideException(
                    "Seule une alerte NOUVELLE peut être prise en charge (statut actuel : "
                            + alerte.getStatut() + ")");
        }

        alerte.setStatut(StatutAlerte.EN_COURS);
        alerte = alerteRepository.save(alerte);
        return alerteMapper.toResponse(alerte);
    }

    @Override
    public AlerteResponse resoudre(Long id, ResolutionRequest request) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new AlerteNotFoundException(id));

        if (alerte.getStatut() != StatutAlerte.EN_COURS) {
            throw new TransitionInvalideException(
                    "Seule une alerte EN_COURS peut être résolue (statut actuel : "
                            + alerte.getStatut() + ")");
        }

        alerte.setStatut(StatutAlerte.RESOLUE);
        alerte.setCommentaireResolution(request.getCommentaireResolution());
        alerte.setDateResolution(LocalDateTime.now());
        alerte = alerteRepository.save(alerte);
        return alerteMapper.toResponse(alerte);
    }

    private BigDecimal calculerEcartPourcentage(BigDecimal montant, BigDecimal montantMoyen) {
        if (montantMoyen.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montant.subtract(montantMoyen).abs()
                .divide(montantMoyen, 4, RoundingMode.HALF_UP)
                .multiply(CENT)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private NiveauGravite determinerGravite(BigDecimal ecartPourcentage) {
        BigDecimal seuilEleve = BigDecimal.valueOf(seuilProperties.getSeuilEleve()).divide(CENT, 4, RoundingMode.HALF_UP);
        BigDecimal seuilMoyenne = BigDecimal.valueOf(seuilProperties.getSeuilMoyenne()).divide(CENT, 4, RoundingMode.HALF_UP);

        if (ecartPourcentage.divide(CENT, 4, RoundingMode.HALF_UP).compareTo(seuilEleve) > 0) {
            return NiveauGravite.ELEVEE;
        }
        if (ecartPourcentage.divide(CENT, 4, RoundingMode.HALF_UP).compareTo(seuilMoyenne) > 0) {
            return NiveauGravite.MOYENNE;
        }
        return NiveauGravite.FAIBLE;
    }
}