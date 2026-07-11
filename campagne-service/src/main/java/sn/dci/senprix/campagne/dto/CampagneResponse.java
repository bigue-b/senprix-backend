package sn.dci.senprix.campagne.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO retourné par l'API pour représenter une campagne, incluant la liste
 * des identifiants d'agents affectés et de marchés associés.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampagneResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private List<Long> agentsIds;
    private List<Long> marchesIds;
    private List<Long> produitsIds;
    private LocalDateTime dateCreation;
}
