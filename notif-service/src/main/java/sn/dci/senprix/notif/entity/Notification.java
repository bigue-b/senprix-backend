package sn.dci.senprix.notif.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sn.dci.senprix.notif.enums.CanalNotification;
import sn.dci.senprix.notif.enums.StatutNotification;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant la table notification : conserve la trace
 * de chaque notification envoyée (ou tentée), pour audit et
 * diagnostic en cas d'échec d'envoi.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinataire_email", nullable = false, length = 200)
    private String destinataireEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotification canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeNotification type;

    @Column(nullable = false, length = 200)
    private String sujet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @Column(name = "message_erreur", columnDefinition = "TEXT")
    private String messageErreur;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;
}
