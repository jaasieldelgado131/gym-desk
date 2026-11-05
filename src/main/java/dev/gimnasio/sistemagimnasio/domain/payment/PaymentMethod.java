package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** Forma de pago que se ofrece al cobrar y al vender (efectivo, tarjeta, transferencia). */
public record PaymentMethod(Long id, String name, boolean active) {
    public PaymentMethod {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        name = name.strip();
    }
}
