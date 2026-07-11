package sn.dci.senprix.prix.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.prix.client.CampagneServiceClient;
import sn.dci.senprix.prix.client.CampagneVerificationDto;
import sn.dci.senprix.prix.client.ProduitServiceClient;
import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.enums.StatutPrix;
import sn.dci.senprix.prix.event.publisher.PrixEventPublisher;
import sn.dci.senprix.prix.exception.PrixInvalideException;
import sn.dci.senprix.prix.exception.PrixNotFoundException;
import sn.dci.senprix.prix.mapper.PrixMapper;
import sn.dci.senprix.prix.repository.PrixRepository;
import sn.dci.senprix.prix.service.impl.PrixServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrixServiceImplTest {

    @Mock
    private PrixRepository prixRepository;

    @Mock
    private PrixMapper prixMapper;

    @Mock
    private ProduitServiceClient produitServiceClient;

    @Mock
    private CampagneServiceClient campagneServiceClient;

    @Mock
    private PrixEventPublisher prixEventPublisher;

    @InjectMocks
    private PrixServiceImpl prixService;

    private PrixRequest requestValide() {
        return new PrixRequest(
                1L, 2L, 3L, 4L,
                new BigDecimal("500.00"), "kg",
                LocalDate.of(2026, 6, 1), null);
    }

    private CampagneVerificationDto campagneAvecAgent(Long agentId) {
        return new CampagneVerificationDto(3L, "EN_COURS", List.of(agentId), List.of(2L));
    }

    @Test
    void creer_avecProduitEtAgentValides_devraitReussirSansSuspicion() {
        // Given
        PrixRequest request = requestValide();
        Prix entiteAvantSauvegarde = Prix.builder().produitId(1L).marcheId(2L).build();
        Prix entiteSauvegardee = Prix.builder().id(1L).produitId(1L).marcheId(2L)
                .statut(StatutPrix.VALIDE).build();

        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(3L)).thenReturn(Optional.of(campagneAvecAgent(4L)));
        when(prixMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(prixRepository.calculerMoyenne(1L, 2L)).thenReturn(null); // aucun historique
        when(prixRepository.save(entiteAvantSauvegarde)).thenReturn(entiteSauvegardee);
        when(prixMapper.toResponse(entiteSauvegardee))
                .thenReturn(PrixResponse.builder().id(1L).statut("VALIDE").build());

        // When
        PrixResponse result = prixService.creer(request);

        // Then
        assertThat(result.getStatut()).isEqualTo("VALIDE");
        verify(prixEventPublisher, never()).publierPrixSuspect(any(), any(Double.class));
    }

    @Test
    void creer_avecProduitInexistant_devraitLeverExceptionSansPersister() {
        // Given
        PrixRequest request = requestValide();
        when(produitServiceClient.produitExiste(1L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> prixService.creer(request))
                .isInstanceOf(PrixInvalideException.class)
                .hasMessageContaining("Aucun produit");

        verify(prixRepository, never()).save(any());
        verify(campagneServiceClient, never()).obtenirCampagne(any());
    }

    @Test
    void creer_avecCampagneInexistante_devraitLeverException() {
        // Given
        PrixRequest request = requestValide();
        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(3L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> prixService.creer(request))
                .isInstanceOf(PrixInvalideException.class)
                .hasMessageContaining("Aucune campagne");

        verify(prixRepository, never()).save(any());
    }

    @Test
    void creer_avecAgentNonAffecteALaCampagne_devraitLeverException() {
        // Given : la campagne existe mais l'agent 4 n'y est pas affecté (seul l'agent 99 l'est)
        PrixRequest request = requestValide();
        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(3L)).thenReturn(Optional.of(campagneAvecAgent(99L)));

        // When / Then
        assertThatThrownBy(() -> prixService.creer(request))
                .isInstanceOf(PrixInvalideException.class)
                .hasMessageContaining("n'est pas affecté");

        verify(prixRepository, never()).save(any());
    }

    @Test
    void creer_avecVariationAnormale_devraitMarquerSuspectEtPublierEvenement() {
        // Given : moyenne actuelle de 200, nouveau prix de 500 -> écart de 150% > seuil 50%
        PrixRequest request = requestValide(); // montant = 500.00
        Prix entiteAvantSauvegarde = Prix.builder().produitId(1L).marcheId(2L).build();
        Prix entiteSauvegardee = Prix.builder().id(1L).produitId(1L).marcheId(2L)
                .montant(new BigDecimal("500.00")).statut(StatutPrix.SUSPECT).build();

        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(3L)).thenReturn(Optional.of(campagneAvecAgent(4L)));
        when(prixMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(prixRepository.calculerMoyenne(1L, 2L)).thenReturn(200.0);
        when(prixRepository.save(entiteAvantSauvegarde)).thenReturn(entiteSauvegardee);
        when(prixMapper.toResponse(entiteSauvegardee))
                .thenReturn(PrixResponse.builder().id(1L).statut("SUSPECT").build());

        // When
        PrixResponse result = prixService.creer(request);

        // Then
        assertThat(result.getStatut()).isEqualTo("SUSPECT");
        assertThat(entiteAvantSauvegarde.getStatut()).isEqualTo(StatutPrix.SUSPECT);
        verify(prixEventPublisher, times(1)).publierPrixSuspect(eq(entiteAvantSauvegarde), eq(200.0));
    }

    @Test
    void creer_avecVariationModeree_nePasMarquerSuspect() {
        // Given : moyenne de 480, nouveau prix de 500 -> écart de ~4%, bien sous le seuil
        PrixRequest request = requestValide(); // montant = 500.00
        Prix entiteAvantSauvegarde = Prix.builder().produitId(1L).marcheId(2L).build();
        Prix entiteSauvegardee = Prix.builder().id(1L).produitId(1L).marcheId(2L)
                .statut(StatutPrix.VALIDE).build();

        when(produitServiceClient.produitExiste(1L)).thenReturn(true);
        when(campagneServiceClient.obtenirCampagne(3L)).thenReturn(Optional.of(campagneAvecAgent(4L)));
        when(prixMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(prixRepository.calculerMoyenne(1L, 2L)).thenReturn(480.0);
        when(prixRepository.save(entiteAvantSauvegarde)).thenReturn(entiteSauvegardee);
        when(prixMapper.toResponse(entiteSauvegardee))
                .thenReturn(PrixResponse.builder().id(1L).statut("VALIDE").build());

        // When
        PrixResponse result = prixService.creer(request);

        // Then
        assertThat(result.getStatut()).isEqualTo("VALIDE");
        verify(prixEventPublisher, never()).publierPrixSuspect(any(), any(Double.class));
    }

    @Test
    void obtenirParId_quandPrixInexistant_devraitLeverException() {
        // Given
        when(prixRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> prixService.obtenirParId(99L))
                .isInstanceOf(PrixNotFoundException.class);
    }

    @Test
    void changerStatut_devraitMettreAJourLeStatut() {
        // Given
        Prix prix = Prix.builder().id(1L).statut(StatutPrix.SUSPECT).build();
        when(prixRepository.findById(1L)).thenReturn(Optional.of(prix));
        when(prixRepository.save(prix)).thenReturn(prix);
        when(prixMapper.toResponse(prix)).thenReturn(PrixResponse.builder().id(1L).statut("REJETE").build());

        // When
        PrixResponse result = prixService.changerStatut(1L, StatutPrix.REJETE);

        // Then
        assertThat(prix.getStatut()).isEqualTo(StatutPrix.REJETE);
        assertThat(result.getStatut()).isEqualTo("REJETE");
    }

    @Test
    void supprimer_quandPrixInexistant_devraitLeverException() {
        // Given
        when(prixRepository.existsById(99L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> prixService.supprimer(99L))
                .isInstanceOf(PrixNotFoundException.class);

        verify(prixRepository, never()).deleteById(any());
    }
}
