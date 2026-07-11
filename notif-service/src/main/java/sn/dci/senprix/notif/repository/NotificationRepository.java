package sn.dci.senprix.notif.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.dci.senprix.notif.entity.Notification;
import sn.dci.senprix.notif.enums.StatutNotification;

import java.util.List;

/**
 * Accès aux données de la table notification.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatut(StatutNotification statut);

    List<Notification> findByDestinataireEmail(String destinataireEmail);
}
