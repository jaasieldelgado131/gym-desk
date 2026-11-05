package dev.gimnasio.sistemagimnasio.infrastructure.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import dev.gimnasio.sistemagimnasio.application.port.out.PasswordHasher;

/** Argon2id; los parámetros quedan dentro del hash, así que cambiarlos no invalida los ya guardados. */
public final class Argon2PasswordHasher implements PasswordHasher {
    private static final int ITERATIONS = 3;
    private static final int MEMORY_KIB = 64 * 1024;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override
    public String hash(String rawPassword) {
        char[] chars = rawPassword.toCharArray();
        try {
            return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        char[] chars = rawPassword.toCharArray();
        try {
            return argon2.verify(passwordHash, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }
}
