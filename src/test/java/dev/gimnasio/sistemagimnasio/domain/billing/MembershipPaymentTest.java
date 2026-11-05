package dev.gimnasio.sistemagimnasio.domain.billing;

import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MembershipPaymentTest {
    private static final Folio FOLIO = Folio.of("GYM-", 15);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 9, 0);
    private static final Plan ESTUDIANTE = new Plan(2L, "Estudiante", 30, Money.of("350"), true);
    private static final PaymentMethod EFECTIVO = new PaymentMethod(1L, "Efectivo", true);

    /** RF-03.3: el monto es siempre el precio del plan. */
    @Test
    void cobra_elPrecioDelPlan_yRegistraSocioPlanMetodoYQuienAtendio() {
        MembershipPayment payment = MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, EFECTIVO, 3);
        assertEquals(new MembershipPayment(null, FOLIO, NOW, 7, 2, 1, Money.of("350.00"), 3), payment);
    }

    /** RF-03.9 */
    @Test
    void planInactivo_noSeCobra() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE.withActive(false), EFECTIVO, 3));
    }

    @Test
    void metodoDePagoInactivo_noSeUsa() {
        PaymentMethod retired = new PaymentMethod(4L, "Vales", false);
        assertThrows(DomainException.class, () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, retired, 3));
    }

    /** RF-03.7: todo cobro lleva un folio único. */
    @Test
    void cobroSinFolio_seRechaza() {
        assertThrows(DomainException.class, () -> MembershipPayment.charge(null, NOW, 7, ESTUDIANTE, EFECTIVO, 3));
    }

    /** Información que se registra: el cobro guarda su fecha. */
    @Test
    void cobroSinFecha_seRechaza() {
        assertThrows(DomainException.class, () -> MembershipPayment.charge(FOLIO, null, 7, ESTUDIANTE, EFECTIVO, 3));
    }
}
