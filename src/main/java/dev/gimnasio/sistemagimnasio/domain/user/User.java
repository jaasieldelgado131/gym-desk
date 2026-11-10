package dev.gimnasio.sistemagimnasio.domain.user;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** La contraseña no forma parte del dominio. */
public record User(Long id, String name, String username, Role role, boolean active) {
    public User {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio.");
        }
        if (username == null || username.isBlank()) {
            throw new DomainException("El nombre de usuario es obligatorio.");
        }
        if (role == null) {
            throw new DomainException("El rol es obligatorio.");
        }
        name = name.strip();
        username = username.strip();
    }

    public void requireActive() {
        if (!active) {
            throw new DomainException("Tu sesión no es válida. Vuelve a iniciar sesión.");
        }
    }

    public void requirePermission(Permission permission) {
        requireActive();
        if (!role.can(permission)) {
            throw new DomainException("Tu rol (" + role.displayName() + ") no tiene permiso para esta acción.");
        }
    }
}
