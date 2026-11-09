package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public record Folio(String value) {
    public static final int DEFAULT_WIDTH = 4;

    public Folio {
        if (value == null || value.isBlank()) {
            throw new DomainException("El folio es obligatorio.");
        }
    }

    public static Folio of(String prefix, long sequence) {
        return of(prefix, sequence, DEFAULT_WIDTH);
    }

    /** Rellena el consecutivo con ceros hasta {@code width} dígitos; si es más largo se escribe completo. */
    public static Folio of(String prefix, long sequence, int width) {
        if (prefix == null || prefix.isBlank()) {
            throw new DomainException("El prefijo del folio es obligatorio.");
        }
        if (sequence < 1) {
            throw new DomainException("El consecutivo del folio debe ser de al menos 1.");
        }
        if (width < 1) {
            throw new DomainException("El ancho del folio debe ser de al menos 1 dígito.");
        }
        return new Folio(prefix + String.format("%0" + width + "d", sequence));
    }
}
