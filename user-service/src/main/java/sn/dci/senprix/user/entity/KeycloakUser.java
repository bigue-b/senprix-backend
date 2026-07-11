package sn.dci.senprix.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table keycloak_user.
 * Assure la traçabilité locale de la synchronisation entre SEN-PRIX
 * et le Realm Keycloak senprix (création, mise à jour de rôle).
 */
@Entity
@Table(name = "keycloak_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeycloakUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 255)
    private String keycloakId;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String realm = "senprix";

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "date_synchronisation", nullable = false)
    private LocalDateTime dateSynchronisation;
}
