package sn.dci.senprix.produit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dci.senprix.produit.dto.ProduitRequest;
import sn.dci.senprix.produit.dto.ProduitResponse;
import sn.dci.senprix.produit.entity.Produit;
import sn.dci.senprix.produit.enums.StatutEnum;
import sn.dci.senprix.produit.exception.ProduitNotFoundException;
import sn.dci.senprix.produit.mapper.ProduitMapper;
import sn.dci.senprix.produit.repository.ProduitRepository;
import sn.dci.senprix.produit.service.impl.ProduitServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceImplTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private ProduitMapper produitMapper;

    @InjectMocks
    private ProduitServiceImpl produitService;

    @Test
    void creer_devraitGenererUnCodeProduitSequentielEtSauvegarder() {
        // Given
        ProduitRequest request = new ProduitRequest("Mil 1kg", "Céréales", "Kg", null);
        Produit entiteAvantSauvegarde = Produit.builder()
                .nom("Mil 1kg").categorie("Céréales").unite("Kg").statut(StatutEnum.ACTIF)
                .build();
        Produit entiteSauvegardee = Produit.builder()
                .id(4L).codeProduit("PRD-004").nom("Mil 1kg")
                .categorie("Céréales").unite("Kg").statut(StatutEnum.ACTIF)
                .build();

        when(produitMapper.toEntity(request)).thenReturn(entiteAvantSauvegarde);
        when(produitRepository.count()).thenReturn(3L); // 3 produits existants → le 4e
        when(produitRepository.save(any(Produit.class))).thenReturn(entiteSauvegardee);
        when(produitMapper.toResponse(entiteSauvegardee))
                .thenReturn(ProduitResponse.builder().id(4L).codeProduit("PRD-004").build());

        // When
        ProduitResponse result = produitService.creer(request);

        // Then
        assertThat(result.getCodeProduit()).isEqualTo("PRD-004");
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void obtenirParId_quandProduitExiste_devraitRetournerLeProduit() {
        // Given
        Produit produit = Produit.builder().id(1L).nom("Riz brisé 25kg").build();
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitMapper.toResponse(produit))
                .thenReturn(ProduitResponse.builder().id(1L).nom("Riz brisé 25kg").build());

        // When
        ProduitResponse result = produitService.obtenirParId(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Riz brisé 25kg");
    }

    @Test
    void obtenirParId_quandProduitInexistant_devraitLeverException() {
        // Given
        when(produitRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> produitService.obtenirParId(99L))
                .isInstanceOf(ProduitNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listerActifs_devraitRetournerSeulementLesProduitsActifs() {
        // Given
        Produit produitActif = Produit.builder().id(1L).statut(StatutEnum.ACTIF).build();
        when(produitRepository.findByStatut(StatutEnum.ACTIF)).thenReturn(List.of(produitActif));
        when(produitMapper.toResponse(produitActif)).thenReturn(ProduitResponse.builder().id(1L).build());

        // When
        List<ProduitResponse> result = produitService.listerActifs();

        // Then
        assertThat(result).hasSize(1);
        verify(produitRepository, times(1)).findByStatut(StatutEnum.ACTIF);
    }

    @Test
    void desactiver_devraitChangerLeStatutEnInactif() {
        // Given
        Produit produit = Produit.builder().id(1L).statut(StatutEnum.ACTIF).build();
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        produitService.desactiver(1L);

        // Then
        assertThat(produit.getStatut()).isEqualTo(StatutEnum.INACTIF);
        verify(produitRepository, times(1)).save(produit);
    }

    @Test
    void desactiver_quandProduitInexistant_devraitLeverException() {
        // Given
        when(produitRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> produitService.desactiver(99L))
                .isInstanceOf(ProduitNotFoundException.class);

        verify(produitRepository, never()).save(any());
    }
}
