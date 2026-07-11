package sn.dci.senprix.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.dci.senprix.user.enums.RoleEnum;

/**
 * DTO reçu en entrée lors de la création d'un agent ou d'un administrateur
 * DCI par un administrateur déjà authentifié (écran 2.8 du mémoire).
 * Le mot de passe n'est volontairement pas demandé ici : il est généré
 * automatiquement et communiqué via le canal sécurisé approprié.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 255)
    private String email;

    @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleEnum role;

    /**
     * Obligatoire uniquement si role == AGENT_COLLECTE.
     * La validation croisée est effectuée dans le service, pas ici,
     * car elle dépend de la valeur d'un autre champ.
     */
    @Size(max = 50)
    private String matricule;

    @Size(max = 200)
    private String zoneAffectation;
}
