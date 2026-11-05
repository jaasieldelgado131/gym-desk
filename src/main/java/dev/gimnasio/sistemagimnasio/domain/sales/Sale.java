package dev.gimnasio.sistemagimnasio.domain.sales;

import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.time.LocalDateTime;
import java.util.List;

public record Sale(
        Long id,
        Folio folio,
        LocalDateTime dateTime,
        List<SaleLine> lines,
        long paymentMethodId,
        long userId) {

    public Sale {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("La venta debe tener al menos un producto.");
        }
        if (dateTime == null) {
            throw new DomainException("La venta debe tener su fecha de creacion.");
        }
        lines = List.copyOf(lines);
    }

    public Money total() {
        return lines.stream().map(SaleLine::subtotal).reduce(Money.ZERO, Money::plus);
    }
}
