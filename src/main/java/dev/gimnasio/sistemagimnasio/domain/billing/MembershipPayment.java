package dev.gimnasio.sistemagimnasio.domain.billing;

import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * El monto es siempre el precio del plan; no se edita.
 * {@code cashReceived} solo existe si el método es efectivo.
 */
public record MembershipPayment(
        Long id,
        Folio folio,
        LocalDateTime dateTime,
        long memberId,
        long planId,
        long paymentMethodId,
        Money amount,
        Money cashReceived,
        long userId) {

    static final String CASH_REQUIRED = "El efectivo recibido es obligatorio.";
    static final String CASH_SHORT = "El efectivo recibido es menor al total.";

    public MembershipPayment {
        if (folio == null) {
            throw new DomainException("El folio es obligatorio.");
        }
        if (dateTime == null) {
            throw new DomainException("La fecha y hora del cobro son obligatorias.");
        }
        if (amount == null) {
            throw new DomainException("El monto es obligatorio.");
        }
        if (cashReceived != null && cashReceived.compareTo(amount) < 0) {
            throw new DomainException(CASH_SHORT);
        }
    }

    /** @param cashReceived obligatorio si el método es efectivo; en los demás se ignora */
    public static MembershipPayment charge(Folio folio, LocalDateTime dateTime, long memberId, Plan plan,
                                           PaymentMethod method, Money cashReceived, long userId) {
        if (!plan.active()) {
            throw new DomainException("El plan está inactivo y no se puede cobrar.");
        }
        if (!method.active()) {
            throw new DomainException("El método de pago está inactivo.");
        }
        if (method.cash() && cashReceived == null) {
            throw new DomainException(CASH_REQUIRED);
        }
        Money received = method.cash() ? cashReceived : null;
        return new MembershipPayment(null, folio, dateTime, memberId, plan.id(), method.id(), plan.price(),
                received, userId);
    }

    /** Vacío si no fue en efectivo. */
    public Optional<Money> change() {
        return Optional.ofNullable(cashReceived).map(received -> received.minus(amount));
    }
}
