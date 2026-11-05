package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** Folio único de un cobro o una venta: prefijo configurable + consecutivo. */
public record Folio(String value) {
    public Folio {
        if (value == null || value.isBlank()) {
            throw new DomainException("El folio es obligatorio.");
        }
    }

    public static Folio of(String prefix, long sequence) {
        return new Folio(prefix + String.format("%06d", sequence));
    }
}
