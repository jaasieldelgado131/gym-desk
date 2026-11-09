package dev.gimnasio.sistemagimnasio.domain.billing;

import dev.gimnasio.sistemagimnasio.domain.payment.CashPayment;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.time.LocalDateTime;

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
        if (cashReceived != null) {
            new CashPayment(amount, cashReceived);
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
        Money received = method.cash() ? new CashPayment(plan.price(), cashReceived).received() : null;
        return new MembershipPayment(null, folio, dateTime, memberId, plan.id(), method.id(), plan.price(),
                received, userId);
    }

    /** null si no fue en efectivo. */
    public Money change() {
        return cashReceived == null ? null : cashReceived.minus(amount);
    }
}
