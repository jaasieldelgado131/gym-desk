package dev.gimnasio.sistemagimnasio.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importe no negativo con dos decimales. La moneda la define GymSettings. */
public record Money(BigDecimal amount) implements Comparable<Money> {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new DomainException("El importe es obligatorio.");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() < 0) {
            throw new DomainException("El importe no puede ser negativo.");
        }
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money times(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }
}
