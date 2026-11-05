package dev.gimnasio.sistemagimnasio.domain.user;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static dev.gimnasio.sistemagimnasio.domain.user.Permission.CHARGE_MEMBERSHIP;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.EDIT_MEMBERS;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.MANAGE_PLANS;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.MANAGE_PRODUCTS;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.MANAGE_SETTINGS;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.MANAGE_USERS;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.REGISTER_STOCK_MOVEMENT;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.SELL;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.VIEW_DAILY_SUMMARY;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.VIEW_INVENTORY;
import static dev.gimnasio.sistemagimnasio.domain.user.Permission.VIEW_MEMBERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tabla de roles de la especificación ("Quiénes lo usan"), fila por fila. */
class RoleTest {

    @Test
    void administrador_tieneAccesoCompletoATodo() {
        assertEquals(EnumSet.allOf(Permission.class), Role.ADMINISTRADOR.permissions());
    }

    @Test
    void recepcion_atiendeElMostrador_peroNoAdministraCatalogosUsuariosNiConfiguracion() {
        Set<Permission> expected = EnumSet.of(
                VIEW_DAILY_SUMMARY,              // Resumen del día: completo
                CHARGE_MEMBERSHIP,               // Cobro de membresías: completo
                SELL,                            // Venta de mostrador: completo
                VIEW_MEMBERS, EDIT_MEMBERS,      // Socios: alta y edición
                VIEW_INVENTORY, REGISTER_STOCK_MOVEMENT); // Inventario: registrar movimientos
        assertEquals(expected, Role.RECEPCION.permissions());
    }

    @Test
    void caja_soloCobraVendeYConsultaSocios() {
        assertEquals(EnumSet.of(CHARGE_MEMBERSHIP, SELL, VIEW_MEMBERS), Role.CAJA.permissions());
        assertFalse(Role.CAJA.can(VIEW_DAILY_SUMMARY));
        assertFalse(Role.CAJA.can(EDIT_MEMBERS));
        assertFalse(Role.CAJA.can(VIEW_INVENTORY));
    }

    @Test
    void soloElAdministrador_administraPlanesProductosUsuariosYConfiguracion() {
        for (Permission adminOnly : EnumSet.of(MANAGE_PLANS, MANAGE_PRODUCTS, MANAGE_USERS, MANAGE_SETTINGS)) {
            for (Role role : Role.values()) {
                assertEquals(role == Role.ADMINISTRADOR, role.can(adminOnly), role + " / " + adminOnly);
            }
        }
    }

    @Test
    void modificarLosPermisosDevueltos_noAmpliaLosDelRol() {
        assertThrows(UnsupportedOperationException.class, () -> Role.CAJA.permissions().add(MANAGE_USERS));
        assertFalse(Role.CAJA.can(MANAGE_USERS));
    }
}
