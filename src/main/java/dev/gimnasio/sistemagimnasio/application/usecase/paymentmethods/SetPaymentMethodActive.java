package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.application.shared.CatalogItem;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

/** Desactivar no cambia los cobros ni las ventas anteriores. */
public final class SetPaymentMethodActive {
    private final PaymentMethodRepository methods;
    private final Authorizer authorizer;

    public SetPaymentMethodActive(PaymentMethodRepository methods, Authorizer authorizer) {
        this.methods = Objects.requireNonNull(methods, "methods");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public CatalogItem execute(User actor, long id, boolean active) {
        authorizer.require(actor, Permission.MANAGE_SETTINGS);
        PaymentMethod current = PaymentMethodRules.find(methods, id);
        if (current.active() == active) {
            return PaymentMethodRules.toItem(current);
        }
        if (!active && methods.countActive() <= 1) {
            throw new DomainException(PaymentMethodRules.LAST_ACTIVE);
        }
        PaymentMethod updated = new PaymentMethod(id, current.name(), current.cash(), active);
        methods.update(updated);
        return PaymentMethodRules.toItem(updated);
    }
}
