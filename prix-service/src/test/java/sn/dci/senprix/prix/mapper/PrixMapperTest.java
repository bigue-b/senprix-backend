package sn.dci.senprix.prix.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.prix.dto.PrixRequest;
import sn.dci.senprix.prix.dto.PrixResponse;
import sn.dci.senprix.prix.entity.Prix;
import sn.dci.senprix.prix.enums.StatutPrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PrixMapperTest {

    private PrixMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PrixMapper();
    }

    @Test
    void toEntity_devraitConvertirRequestEnEntite() {
        // Given
        PrixRequest request = new PrixRequest(
                1L, 2L, 3L, 4L,
                new BigDecimal("500.00"), "kg",
                LocalDate.of(2026, 6, 1), "Marché bien achalandé");

        // When
        Prix entity = mapper.toEntity(request);

        // Then
        assertThat(entity.getProduitId()).isEqualTo(1L);
        assertThat(entity.getMarcheId()).isEqualTo(2L);
        assertThat(entity.getCampagneId()).isEqualTo(3L);
        assertThat(entity.getAgentId()).isEqualTo(4L);
        assertThat(entity.getMontant()).isEqualTo(new BigDecimal("500.00"));
        assertThat(entity.getUnite()).isEqualTo("kg");
    }

    @Test
    void toResponse_devraitConvertirEntiteEnResponse() {
        // Given
        Prix entity = Prix.builder()
                .id(1L)
                .produitId(1L).marcheId(2L).campagneId(3L).agentId(4L)
                .montant(new BigDecimal("500.00"))
                .unite("kg")
                .dateReleve(LocalDate.of(2026, 6, 1))
                .statut(StatutPrix.VALIDE)
                .dateCreation(LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();

        // When
        PrixResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatut()).isEqualTo("VALIDE");
        assertThat(response.getMontant()).isEqualTo(new BigDecimal("500.00"));
    }
}
