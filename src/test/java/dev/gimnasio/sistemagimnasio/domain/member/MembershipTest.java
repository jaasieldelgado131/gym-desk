package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MembershipTest {
    private static final Plan MENSUAL = new Plan(1L, "Mensual", 30, Money.of("450"), true);
    private static final Plan ANUAL = new Plan(4L, "Anual", 365, Money.of("3800"), true);

    @Test
    void elVencimientoEsElInicioMasLosDiasDelPlan_aunqueCruceFinDeMes() {
        Membership m = Membership.begin(7, MENSUAL, LocalDate.of(2026, 1, 31));
        assertEquals(LocalDate.of(2026, 3, 2), m.end());
        assertEquals(7, m.memberId());
        assertEquals(1, m.planId());
    }

    /** La spec define la duración en días: un anual de 365 días que cruza un 29 de febrero vence un día antes del aniversario. */
    @Test
    void unPlanAnualQueCruzaUnAnioBisiesto_dura365DiasNoUnAnioCalendario() {
        assertEquals(LocalDate.of(2028, 2, 29), Membership.begin(1, ANUAL, LocalDate.of(2027, 3, 1)).end());
        assertEquals(LocalDate.of(2029, 2, 28), Membership.begin(1, ANUAL, LocalDate.of(2028, 2, 29)).end());
    }

    @Test
    void elVencimientoDebeSerPosteriorAlInicio() {
        LocalDate day = LocalDate.of(2026, 11, 4);
        assertThrows(DomainException.class, () -> new Membership(null, 1, 1, day, day));
        assertThrows(DomainException.class, () -> new Membership(null, 1, 1, day, day.minusDays(1)));
        assertThrows(DomainException.class, () -> new Membership(null, 1, 1, null, day));
        assertThrows(DomainException.class, () -> new Membership(null, 1, 1, day, null));
    }
}
