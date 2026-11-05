package dev.gimnasio.sistemagimnasio.domain.shared;

/** El mensaje se muestra tal cual al personal. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
