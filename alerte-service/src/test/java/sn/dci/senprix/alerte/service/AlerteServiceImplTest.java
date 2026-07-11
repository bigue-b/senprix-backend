package sn.dci.senprix.alerte.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.alerte.dto.AlerteCreationRequest;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.dto.ResolutionRequest;
import sn.dci.senprix.alerte.entity.Alerte;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;
import sn.dci.senprix.alerte.exception.AlerteNotFoundException;
import sn.dci.senprix.alerte.exception.TransitionInvalideException;
import sn.dci.senprix.alerte.mapper.AlerteMapper;
import sn.dci.senprix.alerte.repository.AlerteRepository;
import sn.dci.senprix.alerte.service.impl.AlerteServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlerteServiceImplTest {

    @Mock
    private AlerteRepository alerteRepository;

    @Mock
    private AlerteMapper alerteMapper;

    @InjectMocks
    private AlerteServiceImpl alerteService;

    @Test
    void creer_avecEcart70Pourcent_devraitClasserFaible() {
        // Given : moyenne 100, montant 170 -> écart de 70%
        AlerteCreationRequest request = new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("170.00"), new BigDecimal("100.00"));

        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alerteMapper.toResponse(any(Alerte.class)))
                .thenReturn(AlerteResponse.builder().niveauGravite("FAIBLE").build());

        ArgumentCaptor<Alerte> captor = ArgumentCaptor.forClass(Alerte.class);

        // When
        alerteService.creer(request);

        // Then
        org.mockito.Mockito.verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getNiveauGravite()).isEqualTo(NiveauGravite.FAIBLE);
        assertThat(captor.getValue().getEcartPourcentage()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void creer_avecEcart150Pourcent_devraitClasserMoyenne() {
        // Given : moyenne 100, montant 250 -> écart de 150%
        AlerteCreationRequest request = new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("250.00"), new BigDecimal("100.00"));

        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alerteMapper.toResponse(any(Alerte.class)))
                .thenReturn(AlerteResponse.builder().niveauGravite("MOYENNE").build());

        ArgumentCaptor<Alerte> captor = ArgumentCaptor.forClass(Alerte.class);

        // When
        alerteService.creer(request);

        // Then
        org.mockito.Mockito.verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getNiveauGravite()).isEqualTo(NiveauGravite.MOYENNE);
    }

    @Test
    void creer_avecEcart250Pourcent_devraitClasserElevee() {
        // Given : moyenne 100, montant 350 -> écart de 250%
        AlerteCreationRequest request = new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("350.00"), new BigDecimal("100.00"));

        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alerteMapper.toResponse(any(Alerte.class)))
                .thenReturn(AlerteResponse.builder().niveauGravite("ELEVEE").build());

        ArgumentCaptor<Alerte> captor = ArgumentCaptor.forClass(Alerte.class);

        // When
        alerteService.creer(request);

        // Then
        org.mockito.Mockito.verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getNiveauGravite()).isEqualTo(NiveauGravite.ELEVEE);
    }

    @Test
    void creer_devraitInitialiserLeStatutANouvelle() {
        // Given
        AlerteCreationRequest request = new AlerteCreationRequest(
                10L, 1L, 1L, new BigDecimal("200.00"), new BigDecimal("100.00"));

        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alerteMapper.toResponse(any(Alerte.class)))
                .thenReturn(AlerteResponse.builder().statut("NOUVELLE").build());

        ArgumentCaptor<Alerte> captor = ArgumentCaptor.forClass(Alerte.class);

        // When
        alerteService.creer(request);

        // Then
        org.mockito.Mockito.verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatut()).isEqualTo(StatutAlerte.NOUVELLE);
    }

    @Test
    void prendreEnCharge_depuisNouvelle_devraitReussir() {
        // Given
        Alerte alerte = Alerte.builder().id(1L).statut(StatutAlerte.NOUVELLE).build();
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));
        when(alerteRepository.save(alerte)).thenReturn(alerte);
        when(alerteMapper.toResponse(alerte))
                .thenReturn(AlerteResponse.builder().statut("EN_COURS").build());

        // When
        AlerteResponse result = alerteService.prendreEnCharge(1L);

        // Then
        assertThat(alerte.getStatut()).isEqualTo(StatutAlerte.EN_COURS);
        assertThat(result.getStatut()).isEqualTo("EN_COURS");
    }

    @Test
    void prendreEnCharge_depuisEnCours_devraitLeverException() {
        // Given : déjà EN_COURS, ne peut pas être pris en charge à nouveau
        Alerte alerte = Alerte.builder().id(1L).statut(StatutAlerte.EN_COURS).build();
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));

        // When / Then
        assertThatThrownBy(() -> alerteService.prendreEnCharge(1L))
                .isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void resoudre_depuisEnCours_devraitReussir() {
        // Given
        Alerte alerte = Alerte.builder().id(1L).statut(StatutAlerte.EN_COURS).build();
        ResolutionRequest request = new ResolutionRequest("Vérifié sur le terrain, erreur de saisie corrigée");

        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));
        when(alerteRepository.save(alerte)).thenReturn(alerte);
        when(alerteMapper.toResponse(alerte))
                .thenReturn(AlerteResponse.builder().statut("RESOLUE").build());

        // When
        AlerteResponse result = alerteService.resoudre(1L, request);

        // Then
        assertThat(alerte.getStatut()).isEqualTo(StatutAlerte.RESOLUE);
        assertThat(alerte.getCommentaireResolution()).isEqualTo("Vérifié sur le terrain, erreur de saisie corrigée");
        assertThat(alerte.getDateResolution()).isNotNull();
        assertThat(result.getStatut()).isEqualTo("RESOLUE");
    }

    @Test
    void resoudre_depuisNouvelle_devraitLeverException() {
        // Given : pas encore prise en charge, ne peut pas être résolue directement
        Alerte alerte = Alerte.builder().id(1L).statut(StatutAlerte.NOUVELLE).build();
        ResolutionRequest request = new ResolutionRequest("Tentative de résolution directe");
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));

        // When / Then
        assertThatThrownBy(() -> alerteService.resoudre(1L, request))
                .isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void obtenirParId_quandAlerteInexistante_devraitLeverException() {
        // Given
        when(alerteRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> alerteService.obtenirParId(99L))
                .isInstanceOf(AlerteNotFoundException.class);
    }
}
