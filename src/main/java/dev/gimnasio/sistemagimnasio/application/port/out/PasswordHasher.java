package dev.gimnasio.sistemagimnasio.application.port.out;

/** Hash de contraseñas (en infraestructura). Las contraseñas solo se guardan como hash. */
public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
