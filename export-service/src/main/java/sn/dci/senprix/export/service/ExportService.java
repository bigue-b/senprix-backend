package sn.dci.senprix.export.service;

import sn.dci.senprix.export.enums.FormatExport;

import java.time.LocalDate;

/**
 * Contrat du service métier d'export des relevés de prix vers un
 * fichier téléchargeable (CSV ou Excel), enrichi avec les noms réels
 * des produits et marchés.
 */
public interface ExportService {

    /**
     * Génère le contenu binaire d'un fichier d'export des relevés de
     * prix correspondant aux critères donnés.
     */
    byte[] exporterPrix(Long produitId, Long marcheId,
                         LocalDate periodeDebut, LocalDate periodeFin,
                         FormatExport format);
}
