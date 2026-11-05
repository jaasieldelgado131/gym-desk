package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public record Phone(String value) {
    public Phone {
        if (value == null || !value.matches("\\d{10}")) {
            throw new DomainException("El teléfono debe tener 10 dígitos.");
        }
    }
}
