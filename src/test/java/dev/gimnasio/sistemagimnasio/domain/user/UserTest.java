package dev.gimnasio.sistemagimnasio.domain.user;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @ParameterizedTest
    @EnumSource(Role.class)
    void usuarioDesactivado_noPuedeHacerNada(Role role) {
        User user = new User(1L, "Ana", "ana", role, false);
        for (Permission permission : Permission.values()) {
            assertThrows(DomainException.class, () -> user.requirePermission(permission), permission::name);
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void usuarioActivo_soloPuedeLoQueSuRolPermite(Role role) {
        User user = new User(1L, "Ana", "ana", role, true);
        for (Permission permission : Permission.values()) {
            if (role.can(permission)) {
                assertDoesNotThrow(() -> user.requirePermission(permission), permission::name);
            } else {
                assertThrows(DomainException.class, () -> user.requirePermission(permission), permission::name);
            }
        }
    }

    @Test
    void nombreYUsuario_seGuardanSinEspaciosAlrededor() {
        User user = new User(null, " Ana López ", " ana ", Role.CAJA, true);
        assertEquals("Ana López", user.name());
        assertEquals("ana", user.username());
    }

    @Test
    void nombreUsuarioYRol_sonObligatorios() {
        assertThrows(DomainException.class, () -> new User(null, " ", "ana", Role.CAJA, true));
        assertThrows(DomainException.class, () -> new User(null, "Ana", "", Role.CAJA, true));
        assertThrows(DomainException.class, () -> new User(null, "Ana", "ana", null, true));
    }
}
