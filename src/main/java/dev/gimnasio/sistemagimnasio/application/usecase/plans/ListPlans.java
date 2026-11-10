package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListPlans {
    private static final Comparator<Plan> ORDER = Comparator.comparing((Plan plan) -> !plan.active())
            .thenComparingInt(Plan::durationDays)
            .thenComparing(plan -> plan.name().toLowerCase());

    private final PlanRepository plans;
    private final Authorizer authorizer;

    public ListPlans(PlanRepository plans, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    /** Primero los activos; en cada grupo, por duración y luego por nombre. */
    public List<PlanRow> execute(User actor) {
        authorizer.require(actor, Permission.MANAGE_PLANS);
        Map<Long, Plan> byId = PlanRules.byId(plans);
        return byId.values().stream().sorted(ORDER).map(plan -> PlanRules.toRow(plan, byId)).toList();
    }
}
