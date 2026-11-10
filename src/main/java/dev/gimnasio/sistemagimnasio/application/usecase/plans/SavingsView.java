package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.domain.member.PlanSavings;
import dev.gimnasio.sistemagimnasio.domain.member.SavingsStatus;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** Ahorro frente a pagar mes a mes; los campos opcionales dependen de {@code status}, como en {@link PlanSavings}. */
public record SavingsView(
        SavingsStatus status,
        Money amount,
        Integer months,
        String referencePlanName,
        Money monthlyTotal,
        Integer percent,
        String reason) {

    public static SavingsView of(PlanSavings savings) {
        return new SavingsView(savings.status(), savings.amount(), savings.months(), savings.referencePlanName(),
                savings.monthlyTotal(), savings.percent(), savings.reason());
    }
}
