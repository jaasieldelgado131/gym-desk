package dev.gimnasio.sistemagimnasio.domain.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void importesConDistintaEscala_sonIgualesYTienenElMismoHash() {
        assertEquals(Money.of("450"), Money.of("450.00"));
        assertEquals(Money.of("450").hashCode(), Money.of("450.0").hashCode());
    }

    @Test
    void redondeaAlCentavo_laMitadHaciaArriba() {
        assertEquals(Money.of("0.01"), Money.of("0.005"));
        assertEquals(Money.of("0.00"), Money.of("0.004"));
    }

    @Test
    void importeNegativo_seRechaza() {
        assertThrows(DomainException.class, () -> Money.of("-0.01"));
    }

    @Test
    void importeNulo_seRechazaConErrorDeDominio() {
        assertThrows(DomainException.class, () -> new Money(null));
    }

    @Test
    void sumarYMultiplicar_noArrastraErroresDeRedondeoBinario() {
        assertEquals(Money.of("0.30"), Money.of("0.10").plus(Money.of("0.20")));
        assertEquals(Money.of("0.30"), Money.of("0.10").times(3));
        assertEquals(Money.ZERO, Money.of("99.99").times(0));
    }

    @Test
    void restarHastaCero_esValido_peroPorDebajoSeRechaza() {
        assertEquals(Money.ZERO, Money.of("450").minus(Money.of("450.00")));
        assertThrows(DomainException.class, () -> Money.of("450").minus(Money.of("450.01")));
    }

    @Test
    void multiplicarPorCantidadNegativa_seRechaza() {
        assertThrows(DomainException.class, () -> Money.of("10").times(-1));
    }

    @Test
    void comparaPorValor_sinImportarLaEscala() {
        assertEquals(0, Money.of("1").compareTo(Money.of("1.00")));
        assertTrue(Money.of("1.00").compareTo(Money.of("1.01")) < 0);
        assertTrue(Money.of("1.01").compareTo(Money.of("1.00")) > 0);
    }
}
