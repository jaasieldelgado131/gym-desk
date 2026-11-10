package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** {@code referenceMonthlyPlanId}: plan de 30 días contra el que se calcula el ahorro; puede ser null. */
public record Plan(Long id, String name, int durationDays, Money price, Long referenceMonthlyPlanId, boolean active) {
    public static final int MAX_DURATION_DAYS = 730;
    public static final int MONTHLY_DAYS = 30;

    public Plan {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del plan es obligatorio.");
        }
        if (durationDays < 1 || durationDays > MAX_DURATION_DAYS) {
            throw new DomainException("La duración del plan debe estar entre 1 y 730 días.");
        }
        if (price == null || price.compareTo(Money.ZERO) <= 0) {
            throw new DomainException("El precio del plan debe ser mayor que cero.");
        }
        if (id != null && id.equals(referenceMonthlyPlanId)) {
            throw new DomainException("Un plan no puede ser su propia referencia mensual.");
        }
        name = name.strip();
    }

    public Plan withActive(boolean active) {
        return new Plan(id, name, durationDays, price, referenceMonthlyPlanId, active);
    }
}
