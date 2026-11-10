package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.application.shared.CatalogCommand;
import dev.gimnasio.sistemagimnasio.application.shared.CatalogItem;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

public final class CreatePaymentMethod {
    private final PaymentMethodRepository methods;
    private final Authorizer authorizer;

    public CreatePaymentMethod(PaymentMethodRepository methods, Authorizer authorizer) {
        this.methods = Objects.requireNonNull(methods, "methods");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** El método nuevo queda activo. */
    public CatalogItem execute(User actor, CatalogCommand command) {
        authorizer.require(actor, Permission.MANAGE_SETTINGS);
        String name = PaymentMethodRules.validName(methods, command.name(), null);
        return PaymentMethodRules.toItem(methods.insert(new PaymentMethod(null, name, command.cash(), true)));
    }
}
