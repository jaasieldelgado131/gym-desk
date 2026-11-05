package dev.gimnasio.sistemagimnasio.domain.shared;

/** Se lanza cuando se viola una regla del negocio; el mensaje está pensado para mostrarse al personal. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
