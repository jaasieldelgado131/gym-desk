package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanSavingsTest {
    private static final Plan MENSUAL = new Plan(1L, "Mensual", 30, Money.of("450"), null, true);

    private static PlanSavings savings(int days, String price, Plan reference) {
        return PlanSavings.of(days, Money.of(price), reference);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 15, 30})
    void unMesOMenos_noAplica(int days) {
        assertEquals(new PlanSavings(SavingsStatus.NO_APLICA, null, null, null, null, null, null),
                savings(days, "450", MENSUAL));
    }

    @Test
    void trimestral_ahorra150_frenteATresMensuales() {
        assertEquals(new PlanSavings(SavingsStatus.AHORRO, Money.of("150"), 3, "Mensual", Money.of("1350"), 11, null),
                savings(90, "1200", MENSUAL));
    }

    @Test
    void anual_cuenta365DiasComoDoceMeses() {
        assertEquals(new PlanSavings(SavingsStatus.AHORRO, Money.of("1600"), 12, "Mensual", Money.of("5400"), 30, null),
                savings(365, "3800", MENSUAL));
    }

    @Test
    void elPorcentaje_seRedondeaHaciaArribaDesdeMedio() {
        // 1350 - 1343.25 = 6.75 → 0.5 % → 1
        assertEquals(1, savings(90, "1343.25", MENSUAL).percent());
    }

    @ParameterizedTest
    @CsvSource({"1350", "1500"})
    void sinAhorro_siCuestaIgualOMasQuePagarMesAMes(String price) {
        assertEquals(new PlanSavings(SavingsStatus.SIN_AHORRO, null, 3, "Mensual", Money.of("1350"), null, null),
                savings(90, price, MENSUAL));
    }

    @Test
    void sinReferencia_noEstaDisponible() {
        assertEquals(PlanSavings.unavailable("el plan no tiene un plan mensual de referencia"), savings(90, "1200", null));
    }

    @Test
    void conLaReferenciaInactiva_noEstaDisponible() {
        assertEquals(PlanSavings.unavailable("el plan de referencia (Mensual) está inactivo"),
                savings(90, "1200", MENSUAL.withActive(false)));
    }

    @Test
    void siLaReferenciaNoDura30Dias_noEstaDisponible() {
        Plan quincenal = new Plan(2L, "Quincenal", 15, Money.of("250"), null, true);
        assertEquals(PlanSavings.unavailable("la referencia no dura 30 días"), savings(90, "1200", quincenal));
    }

    @Test
    void unaDuracionSinMesesCompletos_noEstaDisponible() {
        assertEquals(PlanSavings.unavailable("45 días no equivalen a meses completos"), savings(45, "600", MENSUAL));
    }

    @Test
    void mesesEquivalentes() {
        assertEquals(OptionalInt.of(12), PlanSavings.months(365));
        assertEquals(OptionalInt.of(12), PlanSavings.months(360));
        assertEquals(OptionalInt.of(1), PlanSavings.months(30));
        assertEquals(OptionalInt.of(24), PlanSavings.months(720));
        assertEquals(OptionalInt.empty(), PlanSavings.months(45));
        assertEquals(OptionalInt.empty(), PlanSavings.months(730));
    }
}
