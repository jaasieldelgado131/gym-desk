package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** {@code cash}: al cobrar se pide lo recibido y se calcula el cambio. */
public record PaymentMethod(Long id, String name, boolean cash, boolean active) {
    public PaymentMethod {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        name = name.strip();
    }
}
