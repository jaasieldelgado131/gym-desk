package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

public record Plan(Long id, String name, int durationDays, Money price, boolean active) {
    public Plan {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del plan es obligatorio.");
        }
        if (durationDays < 1) {
            throw new DomainException("La duración del plan debe ser de al menos 1 día.");
        }
        if (price == null) {
            throw new DomainException("El precio del plan es obligatorio.");
        }
        name = name.strip();
    }

    public Plan withActive(boolean active) {
        return new Plan(id, name, durationDays, price, active);
    }
}
