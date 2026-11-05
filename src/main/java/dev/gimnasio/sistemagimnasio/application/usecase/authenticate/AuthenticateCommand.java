package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

/** Lo que la persona escribe en la pantalla de acceso. */
public record AuthenticateCommand(String username, String password) {
    @Override
    public String toString() {
        return "AuthenticateCommand[username=" + username + ", password=***]";
    }
}
