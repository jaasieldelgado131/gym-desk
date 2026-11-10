package dev.gimnasio.sistemagimnasio.application.usecase.gyminfo;

import java.time.LocalDate;
import java.util.Currency;

/** {@code logo} es null si no hay logotipo. */
public record GymInfo(
        String gymName,
        byte[] logo,
        Currency currency,
        LocalDate today,
        int warningDays,
        int autoDeactivationDays) {
}
