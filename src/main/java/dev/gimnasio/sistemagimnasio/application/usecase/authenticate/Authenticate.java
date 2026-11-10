package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

import dev.gimnasio.sistemagimnasio.application.port.out.PasswordHasher;
import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.application.shared.ValidationException;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Authenticate {
    static final String MISSING_USERNAME = "Escribe tu nombre de usuario.";
    static final String MISSING_PASSWORD = "Escribe tu contraseña.";
    static final String INVALID_CREDENTIALS = "Usuario o contraseña incorrectos. Revísalos e inténtalo de nuevo.";
    static final String USER_DISABLED = "Tu usuario está desactivado. Pide a un administrador que lo active.";

    private final UserRepository users;
    private final PasswordHasher hasher;

    public Authenticate(UserRepository users, PasswordHasher hasher) {
        this.users = Objects.requireNonNull(users, "users");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    public User execute(AuthenticateCommand command) {
        String username = command.username() == null ? "" : command.username().strip();
        String password = command.password() == null ? "" : command.password();
        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isEmpty()) {
            errors.put("username", MISSING_USERNAME);
        }
        if (password.isEmpty()) {
            errors.put("password", MISSING_PASSWORD);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        UserCredentials credentials = users.findCredentialsByUsername(username)
                .filter(found -> hasher.matches(password, found.passwordHash()))
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));

        User user = credentials.user();
        if (!user.active()) {
            throw new AuthenticationException(USER_DISABLED);
        }
        return user;
    }
}
