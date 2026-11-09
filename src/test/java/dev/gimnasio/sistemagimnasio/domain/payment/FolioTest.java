package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FolioTest {

    @Test
    void combinaElPrefijoConElConsecutivoRellenoACuatroDigitosPorDefecto() {
        assertEquals("GYM-0042", Folio.of("GYM-", 42).value());
    }

    @Test
    void rellenaAlAnchoIndicado() {
        assertEquals("GYM-000042", Folio.of("GYM-", 42, 6).value());
        assertEquals("GYM-42", Folio.of("GYM-", 42, 1).value());
    }

    @Test
    void consecutivosMasLargosQueElAncho_noSeTruncan() {
        assertEquals("GYM-9999", Folio.of("GYM-", 9_999).value());
        assertEquals("GYM-10000", Folio.of("GYM-", 10_000).value());
    }

    @Test
    void consecutivosDistintos_danFoliosDistintos_tambienAlRebasarElAncho() {
        Set<Folio> folios = new HashSet<>();
        int generated = 0;
        for (long n = 1; n <= 20_000; n++, generated++) {
            folios.add(Folio.of("GYM-", n));
        }
        assertEquals(generated, folios.size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void folioVacio_seRechaza(String value) {
        assertThrows(DomainException.class, () -> new Folio(value));
    }

    /** GymSettings no admite un prefijo vacío; Folio tampoco debería. */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void generarConPrefijoAusente_seRechaza(String prefix) {
        assertThrows(DomainException.class, () -> Folio.of(prefix, 1));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void consecutivoMenorAUno_seRechaza(long sequence) {
        assertThrows(DomainException.class, () -> Folio.of("GYM-", sequence));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void anchoMenorAUno_seRechaza(int width) {
        assertThrows(DomainException.class, () -> Folio.of("GYM-", 1, width));
    }
}
