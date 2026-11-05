package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public class AuthenticationException extends DomainException {
    public AuthenticationException(String message) {
        super(message);
    }
}
