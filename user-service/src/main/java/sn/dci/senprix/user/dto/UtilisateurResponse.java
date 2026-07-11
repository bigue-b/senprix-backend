package sn.dci.senprix.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO retourné par l'API pour représenter un utilisateur SEN-PRIX.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role;
    private Boolean actif;
    private String matricule;
    private String zoneAffectation;
    private LocalDateTime dateCreation;
}
