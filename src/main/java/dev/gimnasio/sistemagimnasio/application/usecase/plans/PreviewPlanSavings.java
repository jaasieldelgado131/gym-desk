package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.application.port.out.PlanRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.member.Plan;
import dev.gimnasio.sistemagimnasio.domain.member.PlanSavings;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

/** Vista previa del formulario: nunca lanza por datos incompletos, solo por permiso. */
public final class PreviewPlanSavings {
    static final String PRICE_MISSING = "escribe un precio mayor que cero para calcular el ahorro";

    private final PlanRepository plans;
    private final Authorizer authorizer;

    public PreviewPlanSavings(PlanRepository plans, Authorizer authorizer) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public SavingsView execute(User actor, PlanCommand command) {
        authorizer.require(actor, Permission.MANAGE_PLANS);
        int days = command.durationDays() == null ? 0 : command.durationDays();
        if (days > Plan.MONTHLY_DAYS && !Plan.isValidPrice(command.price())) {
            return SavingsView.of(PlanSavings.unavailable(PRICE_MISSING));
        }
        Plan reference = command.referenceMonthlyPlanId() == null
                ? null
                : plans.findById(command.referenceMonthlyPlanId()).orElse(null);
        return SavingsView.of(PlanSavings.of(days, command.price(), reference));
    }
}
