package dev.gimnasio.sistemagimnasio.domain.user;

import java.util.EnumSet;
import java.util.Set;

import static dev.gimnasio.sistemagimnasio.domain.user.Permission.*;

public enum Role {
    ADMINISTRADOR(EnumSet.allOf(Permission.class)),
    RECEPCION(EnumSet.of(VIEW_DAILY_SUMMARY, CHARGE_MEMBERSHIP, SELL, VIEW_MEMBERS, EDIT_MEMBERS,
            VIEW_INVENTORY, REGISTER_STOCK_MOVEMENT)),
    CAJA(EnumSet.of(CHARGE_MEMBERSHIP, SELL, VIEW_MEMBERS));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return Set.copyOf(permissions);
    }

    public boolean can(Permission permission) {
        return permissions.contains(permission);
    }
}
