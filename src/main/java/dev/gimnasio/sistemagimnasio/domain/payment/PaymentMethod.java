package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** {@code cash}: al cobrar se pide lo recibido y se calcula el cambio. */
public record PaymentMethod(Long id, String name, boolean cash, boolean active) {
    /** Los mensajes se arman con estos límites; la base repite los mismos en sus CHECK y varchar. */
    public static final int MAX_NAME_LENGTH = 40;

    public PaymentMethod {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        name = name.strip();
        if (!isValidName(name)) {
            throw new DomainException("El nombre debe tener " + MAX_NAME_LENGTH + " caracteres o menos.");
        }
    }

    /** El nombre ya recortado: no vacío y dentro del largo máximo. */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() <= MAX_NAME_LENGTH;
    }
}
