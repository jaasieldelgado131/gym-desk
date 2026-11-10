package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.application.shared.CatalogCommand;
import dev.gimnasio.sistemagimnasio.application.shared.CatalogItem;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

public final class UpdatePaymentMethod {
    private final PaymentMethodRepository methods;
    private final Authorizer authorizer;

    public UpdatePaymentMethod(PaymentMethodRepository methods, Authorizer authorizer) {
        this.methods = Objects.requireNonNull(methods, "methods");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** Cambia nombre y si es efectivo; no cambia si está activo. */
    public CatalogItem execute(User actor, long id, CatalogCommand command) {
        authorizer.require(actor, Permission.MANAGE_SETTINGS);
        PaymentMethod current = PaymentMethodRules.find(methods, id);
        String name = PaymentMethodRules.validName(methods, command.name(), id);
        PaymentMethod updated = new PaymentMethod(id, name, command.cash(), current.active());
        methods.update(updated);
        return PaymentMethodRules.toItem(updated);
    }
}
