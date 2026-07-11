package sn.dci.senprix.notif.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.notif.enums.TypeNotification;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ModeleNotificationTest {

    private ModeleNotification modele;

    @BeforeEach
    void setUp() {
        modele = new ModeleNotification();
    }

    @Test
    void genererSujet_pourAlertePrixSuspect_devraitContenirMotCleAlerte() {
        String sujet = modele.genererSujet(TypeNotification.ALERTE_PRIX_SUSPECT);
        assertThat(sujet).containsIgnoringCase("alerte");
    }

    @Test
    void genererContenu_pourAlertePrixSuspect_devraitInclureLesVariables() {
        Map<String, String> variables = Map.of(
                "produit", "Riz brisé 25kg",
                "marche", "Sandaga",
                "montant", "80000",
                "montantMoyen", "15000",
                "ecartPourcentage", "433.33"
        );

        String contenu = modele.genererContenu(TypeNotification.ALERTE_PRIX_SUSPECT, variables);

        assertThat(contenu).contains("Riz brisé 25kg");
        assertThat(contenu).contains("Sandaga");
        assertThat(contenu).contains("80000");
        assertThat(contenu).contains("433.33");
    }

    @Test
    void genererContenu_avecVariablesManquantes_devraitUtiliserValeurParDefaut() {
        String contenu = modele.genererContenu(TypeNotification.COMPTE_CREE, Map.of());
        assertThat(contenu).contains("N/A");
    }

    @Test
    void genererContenu_pourCompteCree_devraitInclureIdentifiant() {
        Map<String, String> variables = Map.of(
                "nomComplet", "Mamadou Fall",
                "username", "mamadou.fall"
        );

        String contenu = modele.genererContenu(TypeNotification.COMPTE_CREE, variables);

        assertThat(contenu).contains("Mamadou Fall");
        assertThat(contenu).contains("mamadou.fall");
    }

    @Test
    void genererContenu_avecVariablesNulles_nePasLeverException() {
        String contenu = modele.genererContenu(TypeNotification.CAMPAGNE_DEMARREE, null);
        assertThat(contenu).contains("N/A");
    }
}
