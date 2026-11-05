package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.Locale;

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new DomainException("El correo no es válido.");
        }
        value = value.toLowerCase(Locale.ROOT);
    }
}
