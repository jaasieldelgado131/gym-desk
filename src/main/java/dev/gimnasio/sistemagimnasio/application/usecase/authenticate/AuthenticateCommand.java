package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

public record AuthenticateCommand(String username, String password) {
    @Override
    public String toString() {
        return "AuthenticateCommand[username=" + username + ", password=***]";
    }
}
