package dev.gimnasio.sistemagimnasio.domain.billing;

import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MembershipPaymentTest {
    private static final Folio FOLIO = Folio.of("GYM-", 15);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 9, 0);
    private static final Plan ESTUDIANTE = new Plan(2L, "Estudiante", 30, Money.of("350"), true);
    private static final PaymentMethod EFECTIVO = new PaymentMethod(1L, "Efectivo", true, true);
    private static final PaymentMethod TARJETA = new PaymentMethod(2L, "Tarjeta", false, true);

    @Test
    void cobra_elPrecioDelPlan_yRegistraSocioPlanMetodoYQuienAtendio() {
        MembershipPayment payment = MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, TARJETA, null, 3);
        assertEquals(new MembershipPayment(null, FOLIO, NOW, 7, 2, 2, Money.of("350.00"), null, 3), payment);
    }

    @Test
    void enEfectivo_guardaLoRecibido_yCalculaElCambio() {
        MembershipPayment payment = MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, EFECTIVO, Money.of("500"), 3);
        assertEquals(Money.of("500.00"), payment.cashReceived());
        assertEquals(Optional.of(Money.of("150.00")), payment.change());
    }

    @Test
    void enEfectivo_sinLoRecibido_seRechaza() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, EFECTIVO, null, 3));
    }

    @Test
    void enEfectivo_recibidoMenorAlMonto_seRechaza() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, EFECTIVO, Money.of("349.99"), 3));
    }

    @Test
    void enOtroMetodo_loRecibidoSeIgnora() {
        MembershipPayment payment = MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, TARJETA, Money.of("500"), 3);
        assertNull(payment.cashReceived());
        assertEquals(Optional.empty(), payment.change());
    }

    @Test
    void recibidoMenorAlMonto_seRechazaTambienAlReconstruir() {
        assertThrows(DomainException.class,
                () -> new MembershipPayment(1L, FOLIO, NOW, 7, 2, 1, Money.of("350"), Money.of("300"), 3));
    }

    @Test
    void planInactivo_noSeCobra() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE.withActive(false), TARJETA, null, 3));
    }

    @Test
    void metodoDePagoInactivo_noSeUsa() {
        PaymentMethod retired = new PaymentMethod(4L, "Vales", false, false);
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, NOW, 7, ESTUDIANTE, retired, null, 3));
    }

    @Test
    void cobroSinFolio_seRechaza() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(null, NOW, 7, ESTUDIANTE, TARJETA, null, 3));
    }

    @Test
    void cobroSinFecha_seRechaza() {
        assertThrows(DomainException.class,
                () -> MembershipPayment.charge(FOLIO, null, 7, ESTUDIANTE, TARJETA, null, 3));
    }
}
