package dev.gimnasio.sistemagimnasio.domain.inventory;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public record ProductCategory(Long id, String name, boolean active) {
    public ProductCategory {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        name = name.strip();
    }
}
