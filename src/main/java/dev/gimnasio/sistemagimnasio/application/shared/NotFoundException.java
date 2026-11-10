package dev.gimnasio.sistemagimnasio.application.shared;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}
