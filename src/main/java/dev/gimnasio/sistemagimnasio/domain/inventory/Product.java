package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

public record Product(
        Long id,
        String code,
        String name,
        long categoryId,
        Money price,
        int minStock,
        int stock,
        boolean active) {

    public Product {
        if (code == null || code.isBlank()) {
            throw new DomainException("La clave del producto es obligatoria.");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del producto es obligatorio.");
        }
        if (price == null) {
            throw new DomainException("El precio del producto es obligatorio.");
        }
        if (minStock < 0 || stock < 0) {
            throw new DomainException("La existencia no puede ser negativa.");
        }
        code = code.strip();
        name = name.strip();
    }

    public boolean isLow() {
        return stock <= minStock;
    }

    public int stockAfter(MovementType type, int quantity) {
        if (quantity < 0) {
            throw new DomainException("La cantidad no puede ser negativa.");
        }
        int result = switch (type) {
            case ENTRADA -> stock + quantity;
            case SALIDA -> stock - quantity;
            case AJUSTE -> quantity;
        };
        if (result < 0) {
            throw new DomainException("No hay existencia suficiente de \"" + name + "\".");
        }
        return result;
    }

    public Product apply(MovementType type, int quantity) {
        return new Product(id, code, name, categoryId, price, minStock, stockAfter(type, quantity), active);
    }
}
