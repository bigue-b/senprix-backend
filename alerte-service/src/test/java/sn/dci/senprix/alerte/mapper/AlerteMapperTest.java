package sn.dci.senprix.alerte.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.dci.senprix.alerte.dto.AlerteResponse;
import sn.dci.senprix.alerte.entity.Alerte;
import sn.dci.senprix.alerte.enums.NiveauGravite;
import sn.dci.senprix.alerte.enums.StatutAlerte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlerteMapperTest {

    private AlerteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AlerteMapper();
    }

    @Test
    void toResponse_devraitConvertirEntiteEnResponse() {
        // Given
        Alerte entity = Alerte.builder()
                .id(1L)
                .prixId(10L).produitId(1L).marcheId(1L)
                .montant(new BigDecimal("500.00"))
                .montantMoyen(new BigDecimal("200.00"))
                .ecartPourcentage(new BigDecimal("150.00"))
                .niveauGravite(NiveauGravite.MOYENNE)
                .statut(StatutAlerte.NOUVELLE)
                .dateCreation(LocalDateTime.of(2026, 6, 18, 10, 0))
                .build();

        // When
        AlerteResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNiveauGravite()).isEqualTo("MOYENNE");
        assertThat(response.getStatut()).isEqualTo("NOUVELLE");
        assertThat(response.getEcartPourcentage()).isEqualTo(new BigDecimal("150.00"));
    }
}
