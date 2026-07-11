package sn.dci.senprix.export.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Représente un relevé de prix tel que renvoyé par l'API publique du
 * prix-service (GET /api/public/prix). Ce DTO ne circule jamais dans
 * l'API publique du export-service — il sert uniquement à désérialiser
 * la réponse du prix-service pour alimenter la génération des fichiers
 * d'export.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixDto {

    private Long id;
    private Long produitId;
    private Long marcheId;
    private Long campagneId;
    private Long agentId;
    private BigDecimal montant;
    private String unite;
    private LocalDate dateReleve;
    private String statut;
}
