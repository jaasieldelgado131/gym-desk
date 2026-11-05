package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.user.User;

/** Usuario junto con el hash de su contraseña, tal como lo guarda la persistencia. El hash nunca sale de la aplicación. */
public record UserCredentials(User user, String passwordHash) {
    @Override
    public String toString() {
        return "UserCredentials[user=" + user + ", passwordHash=***]";
    }
}
