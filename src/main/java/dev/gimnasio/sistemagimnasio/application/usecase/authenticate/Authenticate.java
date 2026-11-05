package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

import dev.gimnasio.sistemagimnasio.application.port.out.PasswordHasher;
import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

/** RF-01: acceso con usuario y contraseña. Devuelve el usuario con el que se abre la sesión. */
public final class Authenticate {
    static final String MISSING_DATA = "Escribe tu usuario y tu contraseña.";
    static final String INVALID_CREDENTIALS = "El usuario o la contraseña son incorrectos.";

    private final UserRepository users;
    private final PasswordHasher hasher;

    public Authenticate(UserRepository users, PasswordHasher hasher) {
        this.users = Objects.requireNonNull(users, "users");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    public User execute(AuthenticateCommand command) {
        String username = command.username() == null ? "" : command.username().strip();
        String password = command.password() == null ? "" : command.password();
        if (username.isEmpty() || password.isEmpty()) {
            throw new AuthenticationException(MISSING_DATA);
        }

        UserCredentials credentials = users.findCredentialsByUsername(username)
                .filter(found -> hasher.matches(password, found.passwordHash()))
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));

        User user = credentials.user();
        user.requireActive();
        return user;
    }
}
