package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.List;
import java.util.Objects;

public final class ListPaymentMethods {
    private final PaymentMethodRepository methods;
    private final Authorizer authorizer;

    public ListPaymentMethods(PaymentMethodRepository methods, Authorizer authorizer) {
        this.methods = Objects.requireNonNull(methods, "methods");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** Ordenados por nombre. */
    public List<PaymentMethodItem> execute(User actor, boolean includeInactive) {
        authorizer.require(actor, Permission.MANAGE_SETTINGS);
        return methods.findAll(includeInactive).stream().map(PaymentMethodRules::toItem).toList();
    }
}
