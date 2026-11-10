package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolioSeriesTest {

    @Test
    void formatea_conSuPrefijoYSuAncho() {
        assertEquals(new Folio("GYM-0042"), new FolioSeries("COBRO", "GYM-", 4).format(42));
        assertEquals(new Folio("GF-000001"), new FolioSeries("COBRO", "GF-", 6).format(1));
    }

    @Test
    void cambiarElPrefijo_conservaSerieYAncho() {
        assertEquals(new FolioSeries("COBRO", "GF-", 6), new FolioSeries("COBRO", "GYM-", 6).withPrefix("GF-"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"G", "GYM-", "GYM-V-", "ABCD1234", "2026-"})
    void prefijo_de1a8MayusculasNumerosOGuiones_seAcepta(String prefix) {
        assertTrue(FolioSeries.isValidPrefix(prefix));
        assertEquals(prefix, new FolioSeries("COBRO", prefix, 4).prefix());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"gym-", "GYM_", "GYM 1", "ABCDE1234", "ÑU-"})
    void prefijo_conOtrosCaracteresOMasDe8_seRechaza(String prefix) {
        assertFalse(FolioSeries.isValidPrefix(prefix));
        assertThrows(DomainException.class, () -> new FolioSeries("COBRO", prefix, 4));
    }

    @Test
    void serieVacia_oAnchoMenorAUno_seRechazan() {
        assertThrows(DomainException.class, () -> new FolioSeries(" ", "GYM-", 4));
        assertThrows(DomainException.class, () -> new FolioSeries("COBRO", "GYM-", 0));
    }
}
