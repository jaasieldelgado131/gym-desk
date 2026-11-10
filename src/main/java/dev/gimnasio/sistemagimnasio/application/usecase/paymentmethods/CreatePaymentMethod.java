package dev.gimnasio.sistemagimnasio.application.usecase.paymentmethods;

import dev.gimnasio.sistemagimnasio.application.port.out.PaymentMethodRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.payment.PaymentMethod;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

public final class CreatePaymentMethod {
    private final PaymentMethodRepository methods;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public CreatePaymentMethod(PaymentMethodRepository methods, TransactionRunner transactions, Authorizer authorizer) {
        this.methods = Objects.requireNonNull(methods, "methods");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** El método nuevo queda activo. */
    public PaymentMethodItem execute(User actor, PaymentMethodCommand command) {
        return transactions.inTransaction(() -> {
            authorizer.require(actor, Permission.MANAGE_SETTINGS);
            String name = PaymentMethodRules.validName(methods, command.name(), null);
            return PaymentMethodRules.toItem(methods.insert(new PaymentMethod(null, name, command.cash(), true)));
        });
    }
}
