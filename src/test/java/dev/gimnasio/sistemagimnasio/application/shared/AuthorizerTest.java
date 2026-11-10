package dev.gimnasio.sistemagimnasio.application.shared;

import dev.gimnasio.sistemagimnasio.application.fakes.InMemoryUsers;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizerTest {
    private static final String INVALID_SESSION = "Tu sesión no es válida. Vuelve a iniciar sesión.";

    private final InMemoryUsers users = new InMemoryUsers();
    private final Authorizer authorizer = new Authorizer(users);

    @Test
    void conPermiso_devuelveAlUsuarioVigente() {
        User admin = users.admin();
        assertEquals(admin, authorizer.require(admin, Permission.MANAGE_SETTINGS));
    }

    @Test
    void sinPermiso_seRechazaConElNombreDelRol() {
        User caja = users.add("Lucia", Role.CAJA, true);
        DomainException e = assertThrows(DomainException.class, () -> authorizer.require(caja, Permission.MANAGE_SETTINGS));
        assertEquals("Tu rol (Caja) no tiene permiso para esta acción.", e.getMessage());
    }

    @Test
    void desactivadoDuranteLaSesion_seRechaza() {
        User admin = users.admin();
        users.replace(new User(admin.id(), admin.name(), admin.username(), admin.role(), false));

        DomainException e = assertThrows(DomainException.class, () -> authorizer.require(admin, Permission.MANAGE_SETTINGS));
        assertEquals(INVALID_SESSION, e.getMessage());
    }

    @Test
    void rolCambiadoDuranteLaSesion_mandaElRolActual() {
        User admin = users.admin();
        users.replace(new User(admin.id(), admin.name(), admin.username(), Role.RECEPCION, true));

        DomainException e = assertThrows(DomainException.class, () -> authorizer.require(admin, Permission.MANAGE_SETTINGS));
        assertEquals("Tu rol (Recepción) no tiene permiso para esta acción.", e.getMessage());
    }

    @Test
    void usuarioQueYaNoExiste_oSinActor_seRechaza() {
        User ghost = new User(99L, "Nadie", "nadie", Role.ADMINISTRADOR, true);
        assertEquals(INVALID_SESSION,
                assertThrows(DomainException.class, () -> authorizer.require(ghost, Permission.MANAGE_SETTINGS)).getMessage());
        assertEquals(INVALID_SESSION,
                assertThrows(DomainException.class, () -> authorizer.require(null, Permission.MANAGE_SETTINGS)).getMessage());
    }
}
