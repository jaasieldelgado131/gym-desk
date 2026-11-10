package dev.gimnasio.sistemagimnasio.domain.member;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.OptionalInt;

/**
 * Ahorro de un plan frente a pagar mes a mes con su plan mensual de referencia. Si no hay una referencia
 * válida no se inventa: queda {@code NO_DISPONIBLE} con el motivo.
 * {@code amount} y {@code percent} solo existen con {@code AHORRO}; {@code months}, {@code referencePlanName}
 * y {@code monthlyTotal}, con {@code AHORRO} y {@code SIN_AHORRO}.
 */
public record PlanSavings(
        SavingsStatus status,
        Money amount,
        Integer months,
        String referencePlanName,
        Money monthlyTotal,
        Integer percent,
        String reason) {

    private static final int DAYS_IN_YEAR = 365;
    private static final int MONTHS_IN_YEAR = 12;

    /** @param reference el plan mensual de referencia, o null si no tiene */
    public static PlanSavings of(int durationDays, Money price, Plan reference) {
        if (durationDays <= Plan.MONTHLY_DAYS) {
            return new PlanSavings(SavingsStatus.NO_APLICA, null, null, null, null, null, null);
        }
        if (reference == null) {
            return unavailable("el plan no tiene un plan mensual de referencia");
        }
        if (!reference.active()) {
            return unavailable("el plan de referencia (" + reference.name() + ") está inactivo");
        }
        if (reference.durationDays() != Plan.MONTHLY_DAYS) {
            return unavailable("la referencia no dura 30 días");
        }
        OptionalInt months = months(durationDays);
        if (months.isEmpty()) {
            return unavailable(durationDays + " días no equivalen a meses completos");
        }

        Money monthlyTotal = reference.price().times(months.getAsInt());
        BigDecimal saved = monthlyTotal.amount().subtract(price.amount());
        if (saved.signum() <= 0) {
            return new PlanSavings(SavingsStatus.SIN_AHORRO, null, months.getAsInt(), reference.name(), monthlyTotal,
                    null, null);
        }
        int percent = saved.multiply(BigDecimal.valueOf(100))
                .divide(monthlyTotal.amount(), 0, RoundingMode.HALF_UP)
                .intValueExact();
        return new PlanSavings(SavingsStatus.AHORRO, new Money(saved), months.getAsInt(), reference.name(),
                monthlyTotal, percent, null);
    }

    /** Meses completos equivalentes: 365 días son 12; los múltiplos de 30, días ÷ 30; el resto, ninguno. */
    public static OptionalInt months(int days) {
        if (days == DAYS_IN_YEAR) {
            return OptionalInt.of(MONTHS_IN_YEAR);
        }
        return days > 0 && days % Plan.MONTHLY_DAYS == 0 ? OptionalInt.of(days / Plan.MONTHLY_DAYS) : OptionalInt.empty();
    }

    public static PlanSavings unavailable(String reason) {
        return new PlanSavings(SavingsStatus.NO_DISPONIBLE, null, null, null, null, null, reason);
    }
}
