package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** La referencia mensual (id y nombre) es opcional. */
public record PlanRow(
        long id,
        String name,
        int durationDays,
        Money price,
        boolean active,
        Long referenceMonthlyPlanId,
        String referenceMonthlyPlanName,
        SavingsView savings) {
}
