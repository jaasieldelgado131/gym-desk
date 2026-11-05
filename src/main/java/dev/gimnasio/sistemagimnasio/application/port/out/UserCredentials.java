package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.user.User;

/** El hash va aparte del usuario para que nunca llegue a la interfaz. */
public record UserCredentials(User user, String passwordHash) {
    @Override
    public String toString() {
        return "UserCredentials[user=" + user + ", passwordHash=***]";
    }
}
