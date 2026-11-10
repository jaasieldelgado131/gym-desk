package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** {@code referenceMonthlyPlanId}: plan de 30 días contra el que se calcula el ahorro; puede ser null. */
public record Plan(Long id, String name, int durationDays, Money price, Long referenceMonthlyPlanId, boolean active) {
    /** Los mensajes se arman con estos límites; la base repite los mismos en sus CHECK y varchar. */
    public static final int MIN_DURATION_DAYS = 1;
    public static final int MAX_DURATION_DAYS = 730;
    public static final int MAX_NAME_LENGTH = 60;
    public static final int MONTHLY_DAYS = 30;

    public Plan {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del plan es obligatorio.");
        }
        name = name.strip();
        if (!isValidName(name)) {
            throw new DomainException("El nombre del plan debe tener " + MAX_NAME_LENGTH + " caracteres o menos.");
        }
        if (!isValidDuration(durationDays)) {
            throw new DomainException("La duración del plan debe estar entre " + MIN_DURATION_DAYS + " y "
                    + MAX_DURATION_DAYS + " días.");
        }
        if (!isValidPrice(price)) {
            throw new DomainException("El precio del plan debe ser mayor que cero.");
        }
        if (id != null && id.equals(referenceMonthlyPlanId)) {
            throw new DomainException("Un plan no puede ser su propia referencia mensual.");
        }
    }

    /** El nombre ya recortado: no vacío y dentro del largo máximo. */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() <= MAX_NAME_LENGTH;
    }

    public static boolean isValidDuration(Integer days) {
        return days != null && days >= MIN_DURATION_DAYS && days <= MAX_DURATION_DAYS;
    }

    public static boolean isValidPrice(Money price) {
        return price != null && price.compareTo(Money.ZERO) > 0;
    }

    public Plan withActive(boolean active) {
        return new Plan(id, name, durationDays, price, referenceMonthlyPlanId, active);
    }
}
