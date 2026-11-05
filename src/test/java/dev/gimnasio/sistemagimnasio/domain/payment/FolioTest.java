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
    void combinaElPrefijoConElConsecutivoRellenoASeisDigitos() {
        assertEquals("GYM-000042", Folio.of("GYM-", 42).value());
    }

    @Test
    void consecutivosDeMasDeSeisDigitos_noSeTruncan() {
        assertEquals("GYM-999999", Folio.of("GYM-", 999_999).value());
        assertEquals("GYM-1000000", Folio.of("GYM-", 1_000_000).value());
    }

    @Test
    void consecutivosDistintos_danFoliosDistintos_tambienAlCruzarLosSeisDigitos() {
        Set<Folio> folios = new HashSet<>();
        int generated = 0;
        for (long n = 1; n <= 2_000; n++, generated++) {
            folios.add(Folio.of("GYM-", n));
        }
        for (long n = 999_000; n <= 1_001_000; n++, generated++) {
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
}
