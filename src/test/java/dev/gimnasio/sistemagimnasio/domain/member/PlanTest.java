package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanTest {

    @Test
    void laDuracionMinimaEsUnDia() {
        assertEquals(1, new Plan(null, "Día de prueba", 1, Money.of("60"), true).durationDays());
        assertThrows(DomainException.class, () -> new Plan(null, "Día de prueba", 0, Money.of("60"), true));
        assertThrows(DomainException.class, () -> new Plan(null, "Día de prueba", -30, Money.of("60"), true));
    }

    @Test
    void nombreYPrecio_sonObligatorios() {
        assertThrows(DomainException.class, () -> new Plan(null, " ", 30, Money.of("450"), true));
        assertThrows(DomainException.class, () -> new Plan(null, "Mensual", 30, null, true));
    }

    /** RF-07.3: desactivar un plan lo conserva en el historial tal como estaba. */
    @Test
    void desactivar_soloCambiaElEstadoYNoTocaElOriginal() {
        Plan mensual = new Plan(5L, "Mensual", 30, Money.of("450"), true);
        assertEquals(new Plan(5L, "Mensual", 30, Money.of("450"), false), mensual.withActive(false));
        assertTrue(mensual.active());
    }
}
