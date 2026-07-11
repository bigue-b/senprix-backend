package sn.dci.senprix.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sn.dci.senprix.user.entity.Utilisateur;
import sn.dci.senprix.user.enums.RoleEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste les requêtes JPA réelles du UtilisateurRepository sur une base H2 en mémoire.
 */
@DataJpaTest
@ActiveProfiles("test")
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void findByEmail_devraitRetournerUtilisateurCorrespondant() {
        // Given
        Utilisateur utilisateur = Utilisateur.builder()
                .nom("Test").prenom("Test").email("test.repo@dci.sn")
                .role(RoleEnum.ADMIN).actif(true)
                .build();
        utilisateurRepository.save(utilisateur);

        // When
        Optional<Utilisateur> result = utilisateurRepository.findByEmail("test.repo@dci.sn");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Test");
    }

    @Test
    void findByRole_devraitFiltrerCorrectementParRole() {
        // Given
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin1").prenom("Test").email("admin1@dci.sn")
                .role(RoleEnum.ADMIN).actif(true).build());
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Agent1").prenom("Test").email("agent1@dci.sn")
                .role(RoleEnum.AGENT_COLLECTE).actif(true).build());

        // When
        List<Utilisateur> admins = utilisateurRepository.findByRole(RoleEnum.ADMIN);

        // Then
        assertThat(admins).extracting(Utilisateur::getNom).contains("Admin1");
        assertThat(admins).extracting(Utilisateur::getNom).doesNotContain("Agent1");
    }

    @Test
    void existsByEmail_devraitDetecterUnEmailExistant() {
        // Given
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Test").prenom("Test").email("existant@dci.sn")
                .role(RoleEnum.CONSOMMATEUR).actif(true).build());

        // When / Then
        assertThat(utilisateurRepository.existsByEmail("existant@dci.sn")).isTrue();
        assertThat(utilisateurRepository.existsByEmail("inexistant@dci.sn")).isFalse();
    }

    @Test
    void findByActif_devraitFiltrerLesComptesDesactives() {
        // Given
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Actif").prenom("Test").email("actif@dci.sn")
                .role(RoleEnum.ADMIN).actif(true).build());
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Inactif").prenom("Test").email("inactif@dci.sn")
                .role(RoleEnum.ADMIN).actif(false).build());

        // When
        List<Utilisateur> actifs = utilisateurRepository.findByActif(true);

        // Then
        assertThat(actifs).extracting(Utilisateur::getNom).contains("Actif");
        assertThat(actifs).extracting(Utilisateur::getNom).doesNotContain("Inactif");
    }
}
