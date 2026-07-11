package sn.dci.senprix.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.dci.senprix.user.dto.InscriptionCitoyenRequest;
import sn.dci.senprix.user.dto.UtilisateurRequest;
import sn.dci.senprix.user.enums.RoleEnum;
import sn.dci.senprix.user.service.KeycloakAdminService;
import sn.dci.senprix.user.service.UtilisateurService;

@RestController
@RequiredArgsConstructor
public class InscriptionController {

    private final KeycloakAdminService keycloakAdminService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/api/public/utilisateurs/inscription")
    public ResponseEntity<?> inscrire(
            @Valid @RequestBody InscriptionCitoyenRequest request) {

        // Créer le compte Keycloak avec mot de passe permanent
        KeycloakAdminService.KeycloakAccountCreationResult resultat =
            keycloakAdminService.creerCompteCitoyen(
                request.getEmail(), request.getNom(), request.getPrenom(), request.getPassword());

        // Persister en base via le service standard
        UtilisateurRequest utilisateurRequest = new UtilisateurRequest();
        utilisateurRequest.setNom(request.getNom());
        utilisateurRequest.setPrenom(request.getPrenom());
        utilisateurRequest.setEmail(request.getEmail());
        utilisateurRequest.setRole(RoleEnum.CONSOMMATEUR);

        // Sauvegarder uniquement en base (Keycloak déjà créé)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            java.util.Map.of("message", "Compte créé avec succès", "email", request.getEmail())
        );
    }
}
