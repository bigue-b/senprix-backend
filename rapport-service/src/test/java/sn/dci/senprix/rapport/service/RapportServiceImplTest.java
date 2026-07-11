package sn.dci.senprix.rapport.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.rapport.client.PrixDto;
import sn.dci.senprix.rapport.client.PrixServiceClient;
import sn.dci.senprix.rapport.client.ProduitServiceClient;
import sn.dci.senprix.rapport.dto.GenerationRapportRequest;
import sn.dci.senprix.rapport.dto.RapportResponse;
import sn.dci.senprix.rapport.entity.Rapport;
import sn.dci.senprix.rapport.exception.GenerationRapportException;
import sn.dci.senprix.rapport.exception.RapportNotFoundException;
import sn.dci.senprix.rapport.mapper.RapportMapper;
import sn.dci.senprix.rapport.repository.RapportRepository;
import sn.dci.senprix.rapport.service.impl.RapportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RapportServiceImplTest {

    @Mock
    private RapportRepository rapportRepository;

    @Mock
    private RapportMapper rapportMapper;

    @Mock
    private PrixServiceClient prixServiceClient;

    @Mock
    private ProduitServiceClient produitServiceClient;

    @InjectMocks
    private RapportServiceImpl rapportService;

    private GenerationRapportRequest requestValide() {
        return new GenerationRapportRequest(
                1L, 1L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
    }

    private PrixDto creerPrix(BigDecimal montant, LocalDate date, String statut) {
        return new PrixDto(1L, 1L, 1L, 1L, 1L, montant, "kg", date, statut);
    }

    @Test
    void genererSyntheseProduitMarche_avecDonneesValides_devraitCalculerLesAgregations() {
        // Given : 3 relevés VALIDE dans la période (300, 400, 500) -> moyenne 400
        List<PrixDto> prix = List.of(
                creerPrix(new BigDecimal("300"), LocalDate.of(2026, 6, 5), "VALIDE"),
                creerPrix(new BigDecimal("400"), LocalDate.of(2026, 6, 10), "VALIDE"),
                creerPrix(new BigDecimal("500"), LocalDate.of(2026, 6, 15), "VALIDE")
        );

        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz brisé 25kg");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Marché Sandaga");
        when(rapportRepository.save(any(Rapport.class))).thenAnswer(inv -> {
            Rapport r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(rapportMapper.toResponse(any(Rapport.class)))
                .thenReturn(RapportResponse.builder().id(1L).build());

        ArgumentCaptor<Rapport> captor = ArgumentCaptor.forClass(Rapport.class);

        // When
        rapportService.genererSyntheseProduitMarche(requestValide());

        // Then
        org.mockito.Mockito.verify(rapportRepository).save(captor.capture());
        Rapport saved = captor.getValue();
        assertThat(saved.getPrixMoyen()).isEqualByComparingTo("400.00");
        assertThat(saved.getPrixMin()).isEqualByComparingTo("300");
        assertThat(saved.getPrixMax()).isEqualByComparingTo("500");
        assertThat(saved.getNombreReleves()).isEqualTo(3L);
        assertThat(saved.getProduitNom()).isEqualTo("Riz brisé 25kg");
        assertThat(saved.getMarcheNom()).isEqualTo("Marché Sandaga");
    }

    @Test
    void genererSyntheseProduitMarche_devraitExclureLesPrixHorsPeriode() {
        // Given : un relevé dans la période, un autre avant la période
        List<PrixDto> prix = List.of(
                creerPrix(new BigDecimal("300"), LocalDate.of(2026, 6, 10), "VALIDE"),
                creerPrix(new BigDecimal("999"), LocalDate.of(2026, 5, 1), "VALIDE") // hors période
        );

        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(rapportRepository.save(any(Rapport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rapportMapper.toResponse(any(Rapport.class)))
                .thenReturn(RapportResponse.builder().id(1L).build());

        ArgumentCaptor<Rapport> captor = ArgumentCaptor.forClass(Rapport.class);

        // When
        rapportService.genererSyntheseProduitMarche(requestValide());

        // Then
        org.mockito.Mockito.verify(rapportRepository).save(captor.capture());
        assertThat(captor.getValue().getNombreReleves()).isEqualTo(1L);
        assertThat(captor.getValue().getPrixMoyen()).isEqualByComparingTo("300.00");
    }

    @Test
    void genererSyntheseProduitMarche_devraitExclureLesPrixNonValides() {
        // Given : un VALIDE et un SUSPECT dans la même période
        List<PrixDto> prix = List.of(
                creerPrix(new BigDecimal("300"), LocalDate.of(2026, 6, 10), "VALIDE"),
                creerPrix(new BigDecimal("9999"), LocalDate.of(2026, 6, 12), "SUSPECT")
        );

        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(rapportRepository.save(any(Rapport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rapportMapper.toResponse(any(Rapport.class)))
                .thenReturn(RapportResponse.builder().id(1L).build());

        ArgumentCaptor<Rapport> captor = ArgumentCaptor.forClass(Rapport.class);

        // When
        rapportService.genererSyntheseProduitMarche(requestValide());

        // Then
        org.mockito.Mockito.verify(rapportRepository).save(captor.capture());
        assertThat(captor.getValue().getNombreReleves()).isEqualTo(1L);
        assertThat(captor.getValue().getPrixMoyen()).isEqualByComparingTo("300.00");
    }

    @Test
    void genererSyntheseProduitMarche_sansAucunReleveDansLaPeriode_devraitLeverException() {
        // Given
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> rapportService.genererSyntheseProduitMarche(requestValide()))
                .isInstanceOf(GenerationRapportException.class)
                .hasMessageContaining("Aucun relevé");

        org.mockito.Mockito.verify(rapportRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void genererSyntheseProduitMarche_avecPeriodeIncoherente_devraitLeverException() {
        // Given : date de fin avant date de début
        GenerationRapportRequest requestInvalide = new GenerationRapportRequest(
                1L, 1L, LocalDate.of(2026, 6, 30), LocalDate.of(2026, 6, 1));

        // When / Then
        assertThatThrownBy(() -> rapportService.genererSyntheseProduitMarche(requestInvalide))
                .isInstanceOf(GenerationRapportException.class);

        org.mockito.Mockito.verify(prixServiceClient, org.mockito.Mockito.never())
                .listerPrixParProduitEtMarche(any(), any());
    }

    @Test
    void obtenirParId_quandRapportInexistant_devraitLeverException() {
        // Given
        when(rapportRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> rapportService.obtenirParId(99L))
                .isInstanceOf(RapportNotFoundException.class);
    }
}
