package dev.gimnasio.sistemagimnasio.domain.payment;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

/** Pago en efectivo: lo recibido debe cubrir el total y el cambio es la diferencia. */
public record CashPayment(Money total, Money received) {
    public CashPayment {
        if (total == null || received == null) {
            throw new DomainException("El total y el efectivo recibido son obligatorios.");
        }
        if (received.compareTo(total) < 0) {
            throw new DomainException("El efectivo recibido es menor al total.");
        }
    }

    public Money change() {
        return received.minus(total);
    }
}
