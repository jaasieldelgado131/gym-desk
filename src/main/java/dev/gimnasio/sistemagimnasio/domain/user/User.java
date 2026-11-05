package dev.gimnasio.sistemagimnasio.domain.user;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

/** Personal del gimnasio. La contraseña no forma parte del dominio: nunca se muestra. */
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

    /** RF-01.4: un usuario desactivado no puede entrar ni actuar. */
    public void requireActive() {
        if (!active) {
            throw new DomainException("El usuario está desactivado.");
        }
    }

    public void requirePermission(Permission permission) {
        requireActive();
        if (!role.can(permission)) {
            throw new DomainException("El rol (" + role + ") no permite esta acción.");
        }
    }
}
