package dev.gimnasio.sistemagimnasio.domain.billing;

import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.time.LocalDateTime;

/** Cobro de membresía. Siempre se cobra el precio del plan, sin edición ni descuentos. */
public record MembershipPayment(
        Long id,
        Folio folio,
        LocalDateTime dateTime,
        long memberId,
        long planId,
        long paymentMethodId,
        Money amount,
        long userId) {

    public static MembershipPayment charge(Folio folio, LocalDateTime dateTime, long memberId, Plan plan,
                                 PaymentMethod method, long userId) {
        if (!plan.active()) {
            throw new DomainException("El plan está inactivo y no se puede cobrar.");
        }
        if (!method.active()) {
            throw new DomainException("El método de pago está inactivo.");
        }
        return new MembershipPayment(null, folio, dateTime, memberId, plan.id(), method.id(), plan.price(), userId);
    }
}
