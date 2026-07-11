package sn.dci.senprix.prix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.prix.client.CampagneServiceClient;
import sn.dci.senprix.prix.client.CampagneVerificationDto;
import sn.dci.senprix.prix.client.ProduitServiceClient;
import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.dto.StatistiquePrixResponse;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.enums.StatutPrix;
import sn.dci.senprix.prix.event.publisher.PrixEventPublisher;
import sn.dci.senprix.prix.exception.PrixInvalideException;
import sn.dci.senprix.prix.exception.PrixNotFoundException;
import sn.dci.senprix.prix.mapper.PrixMapper;
import sn.dci.senprix.prix.repository.PrixRepository;
import sn.dci.senprix.prix.service.PrixService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implémentation du service métier de gestion des relevés de prix.
 * Avant tout enregistrement, deux appels HTTP synchrones valident
 * l'intégrité référentielle : un vers le produit-service (existence
 * du produit) et un vers le campagne-service (existence de la
 * campagne et appartenance de l'agent déclaré à celle-ci).
 *
 * Un relevé dont le montant s'écarte significativement (au-delà du
 * seuil défini) de la moyenne déjà connue pour ce produit sur ce
 * marché est automatiquement marqué SUSPECT plutôt que rejeté —
 * la décision finale de validation ou de rejet revient à un ADMIN.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PrixServiceImpl implements PrixService {

    /**
     * Seuil de variation (en proportion de la moyenne courante) au-delà
     * duquel un nouveau relevé est automatiquement marqué SUSPECT.
     * Une valeur de 0.5 signifie un écart de plus de 50% par rapport
     * à la moyenne déjà enregistrée pour ce produit sur ce marché.
     */
    private static final BigDecimal SEUIL_VARIATION_SUSPECTE = new BigDecimal("0.5");

    private final PrixRepository prixRepository;
    private final PrixMapper prixMapper;
    private final ProduitServiceClient produitServiceClient;
    private final CampagneServiceClient campagneServiceClient;
    private final PrixEventPublisher prixEventPublisher;

    @Override
    public PrixResponse creer(PrixRequest request) {
        validerProduit(request.getProduitId());
        validerCampagneEtAgent(request.getCampagneId(), request.getAgentId());

        Prix prix = prixMapper.toEntity(request);

        Double moyenneActuelle = prixRepository.calculerMoyenne(request.getProduitId(), request.getMarcheId());
        if (moyenneActuelle != null && estVariationSuspecte(request.getMontant(), moyenneActuelle)) {
            prix.setStatut(StatutPrix.SUSPECT);
            prix = prixRepository.save(prix);
            prixEventPublisher.publierPrixSuspect(prix, moyenneActuelle);
        } else {
            prix = prixRepository.save(prix);
        }

        return prixMapper.toResponse(prix);
    }

    @Override
    @Transactional(readOnly = true)
    public PrixResponse obtenirParId(Long id) {
        Prix prix = prixRepository.findById(id)
                .orElseThrow(() -> new PrixNotFoundException(id));
        return prixMapper.toResponse(prix);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrixResponse> listerTous() {
        return prixRepository.findAll().stream().map(prixMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrixResponse> listerParProduitEtMarche(Long produitId, Long marcheId) {
        return prixRepository.findByProduitIdAndMarcheId(produitId, marcheId)
                .stream().map(prixMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrixResponse> listerParCampagne(Long campagneId) {
        return prixRepository.findByCampagneId(campagneId)
                .stream().map(prixMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrixResponse> listerParAgent(Long agentId) {
        return prixRepository.findByAgentId(agentId)
                .stream().map(prixMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquePrixResponse calculerStatistiques(Long produitId, Long marcheId) {
        Double moyenne = prixRepository.calculerMoyenne(produitId, marcheId);
        BigDecimal min = prixRepository.calculerMinimum(produitId, marcheId);
        BigDecimal max = prixRepository.calculerMaximum(produitId, marcheId);
        long nombre = prixRepository.countByProduitIdAndMarcheIdAndStatut(
                produitId, marcheId, StatutPrix.VALIDE);

        BigDecimal prixMoyen = (moyenne != null)
                ? BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP)
                : null;

        return StatistiquePrixResponse.builder()
                .produitId(produitId)
                .marcheId(marcheId)
                .prixMoyen(prixMoyen)
                .prixMin(min)
                .prixMax(max)
                .nombreReleves(nombre)
                .build();
    }

    @Override
    public PrixResponse changerStatut(Long id, StatutPrix nouveauStatut) {
        Prix prix = prixRepository.findById(id)
                .orElseThrow(() -> new PrixNotFoundException(id));
        prix.setStatut(nouveauStatut);
        prix = prixRepository.save(prix);
        return prixMapper.toResponse(prix);
    }

    @Override
    public void supprimer(Long id) {
        if (!prixRepository.existsById(id)) {
            throw new PrixNotFoundException(id);
        }
        prixRepository.deleteById(id);
    }

    private void validerProduit(Long produitId) {
        if (!produitServiceClient.produitExiste(produitId)) {
            throw new PrixInvalideException("Aucun produit trouvé avec l'identifiant " + produitId);
        }
    }

    private void validerCampagneEtAgent(Long campagneId, Long agentId) {
        CampagneVerificationDto campagne = campagneServiceClient.obtenirCampagne(campagneId)
                .orElseThrow(() -> new PrixInvalideException(
                        "Aucune campagne trouvée avec l'identifiant " + campagneId));

        if (campagne.getAgentsIds() == null || !campagne.getAgentsIds().contains(agentId)) {
            throw new PrixInvalideException(
                    "L'agent " + agentId + " n'est pas affecté à la campagne " + campagneId);
        }
    }

    private boolean estVariationSuspecte(BigDecimal nouveauMontant, double moyenneActuelle) {
        BigDecimal moyenne = BigDecimal.valueOf(moyenneActuelle);
        if (moyenne.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal ecart = nouveauMontant.subtract(moyenne).abs();
        BigDecimal proportionEcart = ecart.divide(moyenne, 4, RoundingMode.HALF_UP);

        return proportionEcart.compareTo(SEUIL_VARIATION_SUSPECTE) > 0;
    }
}
