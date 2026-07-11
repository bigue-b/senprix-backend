package sn.dci.senprix.rapport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO résumé d'un rapport, utilisé pour la liste — exclut le contenu
 * détaillé JSON, potentiellement volumineux, pour alléger la réponse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportResumeResponse {

    private Long id;
    private String type;
    private String titre;
    private String produitNom;
    private String marcheNom;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private LocalDateTime dateGeneration;
}
