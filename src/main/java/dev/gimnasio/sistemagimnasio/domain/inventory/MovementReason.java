package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** Motivo de movimiento de inventario (compra, devolución, conteo físico). */
public record MovementReason(Long id, String name, boolean active) {
    public MovementReason {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        name = name.strip();
    }
}
