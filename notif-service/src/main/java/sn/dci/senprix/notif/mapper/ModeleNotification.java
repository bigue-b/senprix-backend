package sn.dci.senprix.notif.mapper;

import org.springframework.stereotype.Component;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.util.Map;
import java.util.Objects;

/**
 * Génère le sujet et le contenu d'une notification à partir de modèles
 * prédéfinis associés à chaque TypeNotification, et des variables
 * fournies par l'appelant. Centralise la rédaction des messages en un
 * seul endroit, plutôt que de la déléguer à chaque microservice appelant.
 */
@Component
public class ModeleNotification {

    public String genererSujet(TypeNotification type) {
        return switch (type) {
            case ALERTE_PRIX_SUSPECT -> "SEN-PRIX — Alerte : prix suspect détecté";
            case COMPTE_CREE -> "SEN-PRIX — Votre compte a été créé";
            case CAMPAGNE_DEMARREE -> "SEN-PRIX — Une campagne de collecte a démarré";
        };
    }

    public String genererContenu(TypeNotification type, Map<String, String> variables) {
        Map<String, String> vars = (variables != null) ? variables : Map.of();

        return switch (type) {
            case ALERTE_PRIX_SUSPECT -> """
                    Une variation de prix anormale a été détectée.

                    Produit : %s
                    Marché : %s
                    Montant relevé : %s FCFA
                    Moyenne du marché : %s FCFA
                    Écart : %s%%

                    Merci de vérifier cette alerte dans le tableau de bord d'administration.
                    """.formatted(
                    valeurOuDefaut(vars, "produit"),
                    valeurOuDefaut(vars, "marche"),
                    valeurOuDefaut(vars, "montant"),
                    valeurOuDefaut(vars, "montantMoyen"),
                    valeurOuDefaut(vars, "ecartPourcentage"));

            case COMPTE_CREE -> """
                    Bonjour %s,

                    Votre compte SEN-PRIX a été créé avec succès.
                    Identifiant : %s

                    Un mot de passe temporaire vous a été communiqué séparément.
                    Merci de le modifier dès votre première connexion.
                    """.formatted(
                    valeurOuDefaut(vars, "nomComplet"),
                    valeurOuDefaut(vars, "username"));

            case CAMPAGNE_DEMARREE -> """
                    Bonjour,

                    La campagne de collecte "%s" a démarré.
                    Période : du %s au %s

                    Merci de vous référer aux marchés qui vous ont été affectés.
                    """.formatted(
                    valeurOuDefaut(vars, "nomCampagne"),
                    valeurOuDefaut(vars, "dateDebut"),
                    valeurOuDefaut(vars, "dateFin"));
        };
    }

    private String valeurOuDefaut(Map<String, String> variables, String cle) {
        return Objects.requireNonNullElse(variables.get(cle), "N/A");
    }
}
