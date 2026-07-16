package sn.dci.senprix.campagne.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.campagne.client.ProduitServiceClient;
import sn.dci.senprix.campagne.client.UserServiceClient;
import sn.dci.senprix.campagne.client.UtilisateurVerificationDto;
import sn.dci.senprix.campagne.dto.CampagneRequest;
import sn.dci.senprix.campagne.dto.CampagneResponse;
import sn.dci.senprix.campagne.entity.Campagne;
import sn.dci.senprix.campagne.entity.CampagneAgent;
import sn.dci.senprix.campagne.enums.StatutCampagne;
import sn.dci.senprix.campagne.exception.AffectationInvalideException;
import sn.dci.senprix.campagne.exception.CampagneNotFoundException;
import sn.dci.senprix.campagne.mapper.CampagneMapper;
import sn.dci.senprix.campagne.repository.CampagneAgentRepository;
import sn.dci.senprix.campagne.repository.CampagneMarcheRepository;
import sn.dci.senprix.campagne.repository.CampagneProduitRepository;
import sn.dci.senprix.campagne.repository.CampagneRepository;
import sn.dci.senprix.campagne.service.impl.CampagneServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampagneServiceImplTest {

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private CampagneAgentRepository campagneAgentRepository;

    @Mock
    private CampagneMarcheRepository campagneMarcheRepository;

    @Mock
    private CampagneProduitRepository campagneProduitRepository;

    @Mock
    private CampagneMapper campagneMapper;

    @Mock
    private ProduitServiceClient produitServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CampagneServiceImpl campagneService;

    @Test
    void creer_avecDatesCoherentes_devraitCreerLaCampagne() {
        // Given
        CampagneRequest request = new CampagneRequest(
                "Campagne Test", "Description",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Campagne entiteAvantSauvegarde = Campagne.builder().nom("Campagne Test").build();
        Campagne entiteSauvegardee = Campagne.builder().id(1L).nom("Campagne Test").build();

        when(campagneMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(campagneRepository.save(entiteAvantSauvegarde)).thenReturn(entiteSauvegardee);
        when(campagneMapper.toResponse(entiteSauvegardee, List.of(), List.of(), List.of()))
                .thenReturn(CampagneResponse.builder().id(1L).build());

        // When
        CampagneResponse result = campagneService.creer(request);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(campagneRepository, times(1)).save(entiteAvantSauvegarde);
    }

    @Test
    void creer_avecDateFinAvantDateDebut_devraitLeverException() {
        // Given
        CampagneRequest request = new CampagneRequest(
                "Campagne Invalide", null,
                LocalDate.of(2026, 7, 31), LocalDate.of(2026, 7, 1));

        // When / Then
        assertThatThrownBy(() -> campagneService.creer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date de fin");

        verify(campagneRepository, never()).save(any());
    }

    @Test
    void affecterAgent_avecAgentValideEtActif_devraitReussir() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneAgentRepository.existsByCampagneIdAndAgentId(1L, 10L)).thenReturn(false);
        when(userServiceClient.verifierUtilisateur(10L))
                .thenReturn(new UtilisateurVerificationDto(true, "AGENT_COLLECTE", true));
        when(campagneAgentRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMarcheRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneProduitRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMapper.toResponse(campagne, List.of(), List.of(), List.of()))
                .thenReturn(CampagneResponse.builder().id(1L).build());

        // When
        CampagneResponse result = campagneService.affecterAgent(1L, 10L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(campagneAgentRepository, times(1)).save(any(CampagneAgent.class));
    }

    @Test
    void affecterAgent_avecAgentInexistant_devraitLeverExceptionSansPersister() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneAgentRepository.existsByCampagneIdAndAgentId(1L, 999L)).thenReturn(false);
        when(userServiceClient.verifierUtilisateur(999L))
                .thenReturn(new UtilisateurVerificationDto(false, null, false));

        // When / Then
        assertThatThrownBy(() -> campagneService.affecterAgent(1L, 999L))
                .isInstanceOf(AffectationInvalideException.class)
                .hasMessageContaining("Aucun utilisateur");

        verify(campagneAgentRepository, never()).save(any());
    }

    @Test
    void affecterAgent_avecRoleIncorrect_devraitLeverException() {
        // Given : l'utilisateur existe mais est ADMIN, pas AGENT_COLLECTE
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneAgentRepository.existsByCampagneIdAndAgentId(1L, 5L)).thenReturn(false);
        when(userServiceClient.verifierUtilisateur(5L))
                .thenReturn(new UtilisateurVerificationDto(true, "ADMIN", true));

        // When / Then
        assertThatThrownBy(() -> campagneService.affecterAgent(1L, 5L))
                .isInstanceOf(AffectationInvalideException.class)
                .hasMessageContaining("AGENT_COLLECTE");

        verify(campagneAgentRepository, never()).save(any());
    }

    @Test
    void affecterAgent_avecAgentInactif_devraitLeverException() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneAgentRepository.existsByCampagneIdAndAgentId(1L, 7L)).thenReturn(false);
        when(userServiceClient.verifierUtilisateur(7L))
                .thenReturn(new UtilisateurVerificationDto(true, "AGENT_COLLECTE", false));

        // When / Then
        assertThatThrownBy(() -> campagneService.affecterAgent(1L, 7L))
                .isInstanceOf(AffectationInvalideException.class)
                .hasMessageContaining("désactivé");

        verify(campagneAgentRepository, never()).save(any());
    }

    @Test
    void affecterAgent_dejaAffecte_devraitLeverExceptionSansAppelerUserService() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneAgentRepository.existsByCampagneIdAndAgentId(1L, 10L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> campagneService.affecterAgent(1L, 10L))
                .isInstanceOf(AffectationInvalideException.class)
                .hasMessageContaining("déjà affecté");

        verify(userServiceClient, never()).verifierUtilisateur(any());
    }

    @Test
    void associerMarche_avecMarcheExistant_devraitReussir() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneMarcheRepository.existsByCampagneIdAndMarcheId(1L, 50L)).thenReturn(false);
        when(produitServiceClient.marcheExiste(50L)).thenReturn(true);
        when(campagneAgentRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMarcheRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneProduitRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMapper.toResponse(campagne, List.of(), List.of(), List.of()))
                .thenReturn(CampagneResponse.builder().id(1L).build());

        // When
        CampagneResponse result = campagneService.associerMarche(1L, 50L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void associerMarche_avecMarcheInexistant_devraitLeverException() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneMarcheRepository.existsByCampagneIdAndMarcheId(1L, 999L)).thenReturn(false);
        when(produitServiceClient.marcheExiste(999L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> campagneService.associerMarche(1L, 999L))
                .isInstanceOf(AffectationInvalideException.class)
                .hasMessageContaining("Aucun marché");
    }

    @Test
    void obtenirParId_quandCampagneInexistante_devraitLeverException() {
        // Given
        when(campagneRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> campagneService.obtenirParId(99L))
                .isInstanceOf(CampagneNotFoundException.class);
    }

    @Test
    void demarrer_devraitChangerStatutEnCours() {
        // Given
        Campagne campagne = Campagne.builder().id(1L).statut(StatutCampagne.PLANIFIEE).build();
        when(campagneRepository.findById(1L)).thenReturn(Optional.of(campagne));
        when(campagneRepository.save(campagne)).thenReturn(campagne);
        when(campagneAgentRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMarcheRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneProduitRepository.findByCampagneId(1L)).thenReturn(List.of());
        when(campagneMapper.toResponse(campagne, List.of(), List.of(), List.of()))
                .thenReturn(CampagneResponse.builder().id(1L).statut("EN_COURS").build());

        // When
        campagneService.demarrer(1L);

        // Then
        assertThat(campagne.getStatut()).isEqualTo(StatutCampagne.EN_COURS);
    }
}
