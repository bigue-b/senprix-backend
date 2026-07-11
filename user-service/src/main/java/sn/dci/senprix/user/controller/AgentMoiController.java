package sn.dci.senprix.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.dci.senprix.user.dto.UtilisateurResponse;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.exception.UtilisateurNotFoundException;
import sn.dci.senprix.user.mapper.UtilisateurMapper;
import sn.dci.senprix.user.repository.UtilisateurRepository;

/**
 * Expose un endpoint permettant à un utilisateur authentifié (agent ou admin)
 * de récupérer son propre profil à partir du token JWT, sans avoir besoin
 * du rôle ADMIN. Utilisé par le frontend pour obtenir l'ID numérique de
 * l'utilisateur connecté (nécessaire pour soumettre un relevé de prix).
 */
@RestController
@RequestMapping("/api/agent/moi")
@RequiredArgsConstructor
public class AgentMoiController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;

    /**
     * Retourne le profil de l'utilisateur connecté en lisant son
     * keycloak_id (claim "sub") depuis le token JWT.
     */
    @GetMapping
    public ResponseEntity<UtilisateurResponse> obtenirMoi(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject(); // claim "sub" = UUID Keycloak

        Utilisateur utilisateur = utilisateurRepository
                .findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UtilisateurNotFoundException(
                        "Aucun utilisateur trouvé pour ce token — synchronisez votre compte."
                ));

        return ResponseEntity.ok(utilisateurMapper.toResponse(utilisateur));
    }
}