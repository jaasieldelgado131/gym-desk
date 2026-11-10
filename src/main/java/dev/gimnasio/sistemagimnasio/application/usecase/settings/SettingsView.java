package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import dev.gimnasio.sistemagimnasio.domain.payment.Folio;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

import java.util.Currency;

/** {@code currencyLocked}, los conteos y {@code folioFormat} son de solo lectura. */
public record SettingsView(
        String gymName,
        String address,
        String phone,
        String receiptFooter,
        byte[] logo,
        Currency currency,
        int warningDays,
        int autoDeactivationDays,
        String folioPrefix,
        boolean currencyLocked,
        int paymentsCount,
        int salesCount,
        String folioFormat) {

    static SettingsView of(GymSettings settings, int paymentsCount, int salesCount, int folioWidth) {
        return new SettingsView(
                settings.gymName(),
                settings.address(),
                settings.phone(),
                settings.receiptFooter(),
                settings.logo(),
                settings.currency(),
                settings.warningDays(),
                settings.graceDays(),
                settings.folioPrefix(),
                paymentsCount + salesCount > 0,
                paymentsCount,
                salesCount,
                Folio.of(settings.folioPrefix(), 1, folioWidth).value() + " en cobros");
    }
}
