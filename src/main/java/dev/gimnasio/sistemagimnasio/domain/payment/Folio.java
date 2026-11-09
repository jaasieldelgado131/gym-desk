package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public record Folio(String value) {
    public Folio {
        if (value == null || value.isBlank()) {
            throw new DomainException("El folio es obligatorio.");
        }
    }

    public static Folio of(String prefix, long sequence) {
        if (prefix == null || prefix.isBlank()) {
            throw new DomainException("El prefijo del folio es obligatorio.");
        }
        if (sequence < 1) {
            throw new DomainException("El consecutivo del folio debe ser de al menos 1.");
        }
        return new Folio(prefix + String.format("%06d", sequence));
    }
}
