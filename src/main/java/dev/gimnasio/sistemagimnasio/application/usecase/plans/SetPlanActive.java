package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

/** Un plan inactivo deja de ofrecerse al cobrar, pero se conserva en el historial. */
public final class SetPlanActive {
    private final PlanRepository plans;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public SetPlanActive(PlanRepository plans, TransactionRunner transactions, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public PlanRow execute(User actor, long id, boolean active) {
        return transactions.inTransaction(() -> {
            authorizer.require(actor, Permission.MANAGE_PLANS);
            Plan current = PlanRules.find(plans, id);
            if (current.active() == active) {
                return PlanRules.toRow(current, plans);
            }
            Plan updated = current.withActive(active);
            plans.update(updated);
            return PlanRules.toRow(updated, plans);
        });
    }
}
