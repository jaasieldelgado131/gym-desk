package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.time.LocalDateTime;

public record StockMovement(
        Long id,
        LocalDateTime dateTime,
        long productId,
        MovementType type,
        int quantity,
        long reasonId,
        long userId) {

    public StockMovement {
        if (type == null) {
            throw new DomainException("El tipo de movimiento es obligatorio.");
        }
        if (quantity < 0 || (quantity == 0 && type != MovementType.AJUSTE)) {
            throw new DomainException("La cantidad del movimiento no es válida.");
        }
    }
}
