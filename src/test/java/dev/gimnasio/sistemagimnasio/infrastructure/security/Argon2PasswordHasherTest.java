package dev.gimnasio.sistemagimnasio.infrastructure.security;

import de.mkammerer.argon2.Argon2Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argon2PasswordHasherTest {
    private final Argon2PasswordHasher hasher = new Argon2PasswordHasher();

    @Test
    void elHash_usaArgon2idConLosParametrosConfiguradosYNoContieneLaContrasena() {
        String hash = hasher.hash("Mostrador#2026");
        assertTrue(hash.startsWith("$argon2id$v=19$m=65536,t=3,p=1$"), hash);
        assertFalse(hash.contains("Mostrador#2026"));
    }

    @Test
    void laMismaContrasena_coincide_yCualquierVariacionNo() {
        String hash = hasher.hash("Mostrador#2026");
        assertTrue(hasher.matches("Mostrador#2026", hash));
        assertFalse(hasher.matches("mostrador#2026", hash));
        assertFalse(hasher.matches("Mostrador#2026 ", hash));
        assertFalse(hasher.matches("Mostrador#202", hash));
    }

    @Test
    void dosHashesDeLaMismaContrasena_sonDistintos_yAmbosCoinciden() {
        String first = hasher.hash("Mostrador#2026");
        String second = hasher.hash("Mostrador#2026");
        assertNotEquals(first, second);
        assertTrue(hasher.matches("Mostrador#2026", first));
        assertTrue(hasher.matches("Mostrador#2026", second));
    }

    /** Si la ñ se perdiera al codificar, "contrasena" coincidiría con "contraseña". */
    @Test
    void contrasenasConAcentosEnesYEmoji_noPierdenCaracteres() {
        String hash = hasher.hash("contraseña ñandú 🔒");
        assertTrue(hasher.matches("contraseña ñandú 🔒", hash));
        assertFalse(hasher.matches("contrasena nandu ?", hash));
        assertFalse(hasher.matches("contraseña ñandú", hash));
    }

    @Test
    void unHashGuardadoConOtrosParametros_sigueVerificandose() {
        String older = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
                .hash(2, 19 * 1024, 1, "Mostrador#2026".toCharArray());
        assertTrue(hasher.matches("Mostrador#2026", older));
        assertFalse(hasher.matches("otra", older));
    }

    @Test
    void unHashIlegible_noCoincide() {
        assertFalse(hasher.matches("Mostrador#2026", "no-es-un-hash"));
    }
}
