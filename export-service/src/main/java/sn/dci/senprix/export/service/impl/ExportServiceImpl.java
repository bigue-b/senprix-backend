package sn.dci.senprix.export.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.dci.senprix.export.client.PrixDto;
import sn.dci.senprix.export.client.PrixServiceClient;
import sn.dci.senprix.export.client.ProduitServiceClient;
import sn.dci.senprix.export.enums.FormatExport;
import sn.dci.senprix.export.exception.ExportImpossibleException;
import sn.dci.senprix.export.service.ExportService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation du service métier d'export. Filtre les relevés bruts
 * du prix-service à la période demandée (le prix-service ne filtre
 * pas par date côté serveur), enrichit avec les noms réels des
 * produits et marchés impliqués, puis délègue la génération du
 * fichier binaire au générateur correspondant au format demandé.
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final PrixServiceClient prixServiceClient;
    private final ProduitServiceClient produitServiceClient;
    private final CsvExportGenerator csvExportGenerator;
    private final XlsxExportGenerator xlsxExportGenerator;

    @Override
    public byte[] exporterPrix(Long produitId, Long marcheId,
                                LocalDate periodeDebut, LocalDate periodeFin,
                                FormatExport format) {

        List<PrixDto> tousLesPrix = prixServiceClient.listerPrixParProduitEtMarche(produitId, marcheId);

        List<PrixDto> prixFiltres = tousLesPrix.stream()
                .filter(prix -> periodeDebut == null || !prix.getDateReleve().isBefore(periodeDebut))
                .filter(prix -> periodeFin == null || !prix.getDateReleve().isAfter(periodeFin))
                .toList();

        if (prixFiltres.isEmpty()) {
            throw new ExportImpossibleException(
                    "Aucun relevé de prix trouvé pour ces critères — export impossible");
        }

        Map<Long, String> nomsProduits = construireMapNomsProduits(prixFiltres);
        Map<Long, String> nomsMarches = construireMapNomsMarches(prixFiltres);

        return switch (format) {
            case CSV -> csvExportGenerator.genererCsv(prixFiltres, nomsProduits, nomsMarches);
            case XLSX -> xlsxExportGenerator.genererXlsx(prixFiltres, nomsProduits, nomsMarches);
        };
    }

    /**
     * Récupère le nom de chaque produit distinct présent dans les
     * relevés, en limitant le nombre d'appels HTTP au strict nécessaire
     * (un seul appel par produit distinct, pas un par relevé).
     */
    private Map<Long, String> construireMapNomsProduits(List<PrixDto> prix) {
        Map<Long, String> noms = new HashMap<>();
        prix.stream()
                .map(PrixDto::getProduitId)
                .distinct()
                .forEach(id -> noms.put(id, produitServiceClient.obtenirNomProduit(id)));
        return noms;
    }

    private Map<Long, String> construireMapNomsMarches(List<PrixDto> prix) {
        Map<Long, String> noms = new HashMap<>();
        prix.stream()
                .map(PrixDto::getMarcheId)
                .distinct()
                .forEach(id -> noms.put(id, produitServiceClient.obtenirNomMarche(id)));
        return noms;
    }
}
