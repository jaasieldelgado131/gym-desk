package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.List;
import java.util.Objects;

/** Solo se borra un plan que nadie usa; uno con membresías se desactiva para conservar el historial. */
public final class DeletePlan {
    private final PlanRepository plans;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public DeletePlan(PlanRepository plans, TransactionRunner transactions, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public void execute(User actor, long id) {
        transactions.run(() -> {
            authorizer.require(actor, Permission.MANAGE_PLANS);
            Plan plan = PlanRules.find(plans, id);
            if (plans.hasMemberships(id)) {
                throw new DomainException("No puedes eliminar «" + plan.name()
                        + "»: ya tiene membresías. Desactívalo para dejar de ofrecerlo.");
            }
            List<String> referencing = plans.namesReferencing(id);
            if (!referencing.isEmpty()) {
                throw new DomainException("No puedes eliminar «" + plan.name() + "»: es la referencia mensual de "
                        + String.join(", ", referencing) + ". Cambia esa referencia antes.");
            }
            plans.delete(id);
        });
    }
}
