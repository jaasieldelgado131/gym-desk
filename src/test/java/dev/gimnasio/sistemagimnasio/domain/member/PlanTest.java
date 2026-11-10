package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanTest {

    private static Plan plan(int days, String price) {
        return new Plan(null, "Plan", days, Money.of(price), null, true);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 730})
    void laDuracion_vaDe1a730Dias(int days) {
        assertEquals(days, plan(days, "60").durationDays());
    }

    @ParameterizedTest
    @ValueSource(ints = {-30, 0, 731})
    void unaDuracionFueraDe1a730_seRechaza(int days) {
        assertThrows(DomainException.class, () -> plan(days, "60"));
    }

    @Test
    void elPrecio_debeSerMayorQueCero() {
        assertEquals(Money.of("0.01"), plan(30, "0.01").price());
        assertThrows(DomainException.class, () -> plan(30, "0"));
    }

    @Test
    void elNombre_tieneComoMaximo60Caracteres_yElMensajeLoDice() {
        assertEquals(Plan.MAX_NAME_LENGTH, new Plan(null, "x".repeat(Plan.MAX_NAME_LENGTH), 30, Money.of("1"), null, true)
                .name().length());
        DomainException e = assertThrows(DomainException.class,
                () -> new Plan(null, "x".repeat(Plan.MAX_NAME_LENGTH + 1), 30, Money.of("1"), null, true));
        assertEquals("El nombre del plan debe tener " + Plan.MAX_NAME_LENGTH + " caracteres o menos.", e.getMessage());
    }

    @Test
    void nombreYPrecio_sonObligatorios() {
        assertThrows(DomainException.class, () -> new Plan(null, " ", 30, Money.of("450"), null, true));
        assertThrows(DomainException.class, () -> new Plan(null, "Mensual", 30, null, null, true));
    }

    @Test
    void unPlan_noPuedeSerSuPropiaReferencia() {
        assertThrows(DomainException.class, () -> new Plan(4L, "Anual", 365, Money.of("3800"), 4L, true));
        assertDoesNotThrow(() -> new Plan(4L, "Anual", 365, Money.of("3800"), 1L, true));
        assertDoesNotThrow(() -> new Plan(null, "Anual", 365, Money.of("3800"), 1L, true));
    }

    @Test
    void desactivar_soloCambiaElEstadoYNoTocaElOriginal() {
        Plan anual = new Plan(5L, "Anual", 365, Money.of("3800"), 1L, true);
        assertEquals(new Plan(5L, "Anual", 365, Money.of("3800"), 1L, false), anual.withActive(false));
        assertTrue(anual.active());
    }
}
