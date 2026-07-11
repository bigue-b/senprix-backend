package sn.dci.senprix.notif.client;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import sn.dci.senprix.notif.exception.EnvoiEmailException;

/**
 * Encapsule l'envoi effectif d'emails via JavaMailSender (protocole
 * SMTP). Isolé dans un composant dédié pour que NotificationServiceImpl
 * n'ait jamais à connaître les détails techniques de la librairie
 * d'envoi sous-jacente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailClient {

    private final JavaMailSender mailSender;

    @Value("${notification.email.expediteur:no-reply@senprix.sn}")
    private String adresseExpediteur;

    public void envoyer(String destinataire, String sujet, String contenu) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(adresseExpediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenu, false);

            mailSender.send(message);
            log.info("Email envoyé avec succès à {} (sujet : {})", destinataire, sujet);

        } catch (MessagingException | RuntimeException ex) {
            log.error("Échec de l'envoi de l'email à {} : {} - {}",
                    destinataire, ex.getClass().getSimpleName(), ex.getMessage());
            throw new EnvoiEmailException(
                    "Impossible d'envoyer l'email à " + destinataire, ex);
        }
    }
}
