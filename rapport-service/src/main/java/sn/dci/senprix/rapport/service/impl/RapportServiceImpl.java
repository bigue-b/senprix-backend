package sn.dci.senprix.rapport.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dci.senprix.rapport.client.PrixDto;
import sn.dci.senprix.rapport.client.PrixServiceClient;
import sn.dci.senprix.rapport.client.ProduitServiceClient;
import sn.dci.senprix.rapport.dto.GenerationRapportRequest;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.dto.RapportResumeResponse;
import sn.dci.senprix.rapport.entity.Rapport;
import sn.dci.senprix.rapport.enums.TypeRapport;
import sn.dci.senprix.rapport.exception.GenerationRapportException;
import sn.dci.senprix.rapport.exception.RapportNotFoundException;
import sn.dci.senprix.rapport.mapper.RapportMapper;
import sn.dci.senprix.rapport.repository.RapportRepository;
import sn.dci.senprix.rapport.service.RapportService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Comparator;

/**
 * Implémentation du service métier de génération de rapports.
 * Filtre les relevés de prix bruts à la période demandée (le
 * prix-service ne filtre pas par date côté serveur, ce filtrage est
 * donc effectué ici), ne retient que les relevés VALIDE pour le
 * calcul des agrégations (les prix SUSPECT ou REJETE ne doivent pas
 * fausser les statistiques présentées), puis enrichit le résultat
 * avec les noms réels du produit et du marché.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RapportServiceImpl implements RapportService {

    private final RapportRepository rapportRepository;
    private final RapportMapper rapportMapper;
    private final PrixServiceClient prixServiceClient;
    private final ProduitServiceClient produitServiceClient;

    @Override
    public RapportResponse genererSyntheseProduitMarche(GenerationRapportRequest request) {
        validerPeriode(request);

        List<PrixDto> tousLesPrix = prixServiceClient.listerPrixParProduitEtMarche(
                request.getProduitId(), request.getMarcheId());

        List<PrixDto> prixDansLaPeriode = tousLesPrix.stream()
                .filter(prix -> "VALIDE".equals(prix.getStatut()))
                .filter(prix -> !prix.getDateReleve().isBefore(request.getPeriodeDebut())
                        && !prix.getDateReleve().isAfter(request.getPeriodeFin()))
                .toList();

        if (prixDansLaPeriode.isEmpty()) {
            throw new GenerationRapportException(
                    "Aucun relevé de prix valide trouvé pour ce produit sur ce marché "
                            + "durant la période demandée");
        }

        String nomProduit = produitServiceClient.obtenirNomProduit(request.getProduitId());
        String nomMarche = produitServiceClient.obtenirNomMarche(request.getMarcheId());

        Rapport rapport = Rapport.builder()
                .type(TypeRapport.EVOLUTION_PRIX_PRODUIT)
                .titre("Évolution du prix de " + nomProduit + " — " + nomMarche)
                .produitId(request.getProduitId())
                .produitNom(nomProduit)
                .marcheId(request.getMarcheId())
                .marcheNom(nomMarche)
                .periodeDebut(request.getPeriodeDebut())
                .periodeFin(request.getPeriodeFin())
                .prixMoyen(calculerMoyenne(prixDansLaPeriode))
                .prixMin(calculerMinimum(prixDansLaPeriode))
                .prixMax(calculerMaximum(prixDansLaPeriode))
                .nombreReleves((long) prixDansLaPeriode.size())
                .contenuDetailleJson(construireContenuDetaille(prixDansLaPeriode))
                .build();

        rapport = rapportRepository.save(rapport);
        return rapportMapper.toResponse(rapport);
    }

    @Override
    @Transactional(readOnly = true)
    public RapportResponse obtenirParId(Long id) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new RapportNotFoundException(id));
        return rapportMapper.toResponse(rapport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RapportResumeResponse> listerTous() {
        return rapportRepository.findAll().stream()
                .map(rapportMapper::toResumeResponse)
                .toList();
    }

    private void validerPeriode(GenerationRapportRequest request) {
        if (request.getPeriodeFin().isBefore(request.getPeriodeDebut())) {
            throw new GenerationRapportException(
                    "La date de fin de période ne peut pas être antérieure à la date de début");
        }
    }

    private BigDecimal calculerMoyenne(List<PrixDto> prix) {
        BigDecimal somme = prix.stream()
                .map(PrixDto::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return somme.divide(BigDecimal.valueOf(prix.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculerMinimum(List<PrixDto> prix) {
        return prix.stream()
                .map(PrixDto::getMontant)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculerMaximum(List<PrixDto> prix) {
        return prix.stream()
                .map(PrixDto::getMontant)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Construit une représentation JSON simple de la série de relevés
     * (date, montant), utilisable côté client pour tracer un graphique
     * d'évolution. Construction manuelle plutôt qu'une dépendance JSON
     * supplémentaire, le format restant volontairement minimal.
     */
    private String construireContenuDetaille(List<PrixDto> prix) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < prix.size(); i++) {
            PrixDto p = prix.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"date\":\"").append(p.getDateReleve())
                    .append("\",\"montant\":").append(p.getMontant())
                    .append("}");
        }
        json.append("]");
        return json.toString();
    }
}
