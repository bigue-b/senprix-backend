package sn.dci.senprix.export.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.export.client.PrixDto;
import sn.dci.senprix.export.client.PrixServiceClient;
import sn.dci.senprix.export.client.ProduitServiceClient;
import sn.dci.senprix.export.enums.FormatExport;
import sn.dci.senprix.export.exception.ExportImpossibleException;
import sn.dci.senprix.export.service.impl.CsvExportGenerator;
import sn.dci.senprix.export.service.impl.ExportServiceImpl;
import sn.dci.senprix.export.service.impl.XlsxExportGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private PrixServiceClient prixServiceClient;

    @Mock
    private ProduitServiceClient produitServiceClient;

    @Mock
    private CsvExportGenerator csvExportGenerator;

    @Mock
    private XlsxExportGenerator xlsxExportGenerator;

    @InjectMocks
    private ExportServiceImpl exportService;

    private PrixDto creerPrix(LocalDate date) {
        return new PrixDto(1L, 1L, 1L, 1L, 1L, new BigDecimal("15000"), "kg", date, "VALIDE");
    }

    @Test
    void exporterPrix_avecFormatCsv_devraitAppelerLeGenerateurCsv() {
        // Given
        List<PrixDto> prix = List.of(creerPrix(LocalDate.of(2026, 6, 15)));
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(csvExportGenerator.genererCsv(any(), any(), any())).thenReturn("contenu csv".getBytes());

        // When
        byte[] resultat = exportService.exporterPrix(1L, 1L, null, null, FormatExport.CSV);

        // Then
        assertThat(resultat).isEqualTo("contenu csv".getBytes());
        verify(csvExportGenerator, times(1)).genererCsv(any(), any(), any());
        verify(xlsxExportGenerator, never()).genererXlsx(any(), any(), any());
    }

    @Test
    void exporterPrix_avecFormatXlsx_devraitAppelerLeGenerateurXlsx() {
        // Given
        List<PrixDto> prix = List.of(creerPrix(LocalDate.of(2026, 6, 15)));
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(xlsxExportGenerator.genererXlsx(any(), any(), any())).thenReturn("contenu xlsx".getBytes());

        // When
        byte[] resultat = exportService.exporterPrix(1L, 1L, null, null, FormatExport.XLSX);

        // Then
        assertThat(resultat).isEqualTo("contenu xlsx".getBytes());
        verify(xlsxExportGenerator, times(1)).genererXlsx(any(), any(), any());
        verify(csvExportGenerator, never()).genererCsv(any(), any(), any());
    }

    @Test
    void exporterPrix_devraitFiltrerParPeriode() {
        // Given : un relevé dans la période, un hors période
        List<PrixDto> prix = List.of(
                creerPrix(LocalDate.of(2026, 6, 15)),
                creerPrix(LocalDate.of(2026, 1, 1))
        );
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(csvExportGenerator.genererCsv(any(), any(), any())).thenReturn(new byte[0]);

        // When
        exportService.exporterPrix(1L, 1L,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), FormatExport.CSV);

        // Then : vérifie que le générateur a reçu une liste filtrée à 1 élément
        verify(csvExportGenerator).genererCsv(argThat(liste -> liste.size() == 1), any(), any());
    }

    @Test
    void exporterPrix_sansAucunReleve_devraitLeverException() {
        // Given
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> exportService.exporterPrix(1L, 1L, null, null, FormatExport.CSV))
                .isInstanceOf(ExportImpossibleException.class);

        verify(csvExportGenerator, never()).genererCsv(any(), any(), any());
    }

    @Test
    void exporterPrix_devraitAppelerProduitServiceUneSeuleFoisParProduitDistinct() {
        // Given : 3 relevés, mais tous le même produit/marché
        List<PrixDto> prix = List.of(
                creerPrix(LocalDate.of(2026, 6, 1)),
                creerPrix(LocalDate.of(2026, 6, 2)),
                creerPrix(LocalDate.of(2026, 6, 3))
        );
        when(prixServiceClient.listerPrixParProduitEtMarche(1L, 1L)).thenReturn(prix);
        when(produitServiceClient.obtenirNomProduit(1L)).thenReturn("Riz");
        when(produitServiceClient.obtenirNomMarche(1L)).thenReturn("Sandaga");
        when(csvExportGenerator.genererCsv(any(), any(), any())).thenReturn(new byte[0]);

        // When
        exportService.exporterPrix(1L, 1L, null, null, FormatExport.CSV);

        // Then : un seul appel par produit/marché distinct, pas un par relevé
        verify(produitServiceClient, times(1)).obtenirNomProduit(1L);
        verify(produitServiceClient, times(1)).obtenirNomMarche(1L);
    }
}
