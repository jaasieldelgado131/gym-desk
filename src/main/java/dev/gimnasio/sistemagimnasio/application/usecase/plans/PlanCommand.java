package dev.gimnasio.sistemagimnasio.application.usecase.plans;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** {@code referenceMonthlyPlanId}: opcional; plan de 30 días contra el que se calcula el ahorro. */
public record PlanCommand(String name, Integer durationDays, Money price, Long referenceMonthlyPlanId) {
}
