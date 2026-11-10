package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import java.util.Currency;

/** {@code logo} en null quita el logotipo; para conservarlo se reenvían los bytes recibidos. */
public record UpdateSettingsCommand(
        String gymName,
        String address,
        String phone,
        String receiptFooter,
        byte[] logo,
        Currency currency,
        Integer warningDays,
        Integer autoDeactivationDays,
        String folioPrefix) {
}
