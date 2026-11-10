package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

public final class CreatePlan {
    private final PlanRepository plans;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public CreatePlan(PlanRepository plans, TransactionRunner transactions, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** El plan nuevo queda activo. */
    public PlanRow execute(User actor, PlanCommand command) {
        return transactions.inTransaction(() -> {
            authorizer.require(actor, Permission.MANAGE_PLANS);
            return PlanRules.toRow(plans.insert(PlanRules.validPlan(command, null, true, plans)), plans);
        });
    }
}
