package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

/** Cambiar el precio solo afecta a los cobros nuevos: los anteriores guardan lo que se pagó. */
public final class UpdatePlan {
    private final PlanRepository plans;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public UpdatePlan(PlanRepository plans, TransactionRunner transactions, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** No cambia si el plan está activo. */
    public PlanRow execute(User actor, long id, PlanCommand command) {
        return transactions.inTransaction(() -> {
            authorizer.require(actor, Permission.MANAGE_PLANS);
            Plan current = PlanRules.find(plans, id);
            Plan updated = PlanRules.validPlan(command, id, current.active(), plans);
            plans.update(updated);
            return PlanRules.toRow(updated, plans);
        });
    }
}
