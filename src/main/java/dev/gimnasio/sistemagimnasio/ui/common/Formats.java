package dev.gimnasio.sistemagimnasio.ui.common;

import dev.gimnasio.sistemagimnasio.domain.shared.Money;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

/** Cómo se muestran importes y fechas en toda la app. Solo da formato: no calcula nada. */
public final class Formats {
    public static final Locale LOCALE = Locale.forLanguageTag("es-MX");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE);
    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", LOCALE);

    private Formats() {
    }

    /** «$1,250.00» en pesos; otras monedas llevan su código o símbolo. */
    public static String money(Money money, Currency currency) {
        NumberFormat format = NumberFormat.getCurrencyInstance(LOCALE);
        format.setCurrency(currency);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(money.amount());
    }

    /** «09/11/2026». */
    public static String date(LocalDate date) {
        return DATE.format(date);
    }

    /** «09/11/2026 18:05». */
    public static String dateTime(LocalDateTime dateTime) {
        return DATE_TIME.format(dateTime);
    }

    /** «Lunes 9 de noviembre de 2026», para encabezados. */
    public static String longDate(LocalDate date) {
        String text = LONG_DATE.format(date);
        return text.substring(0, 1).toUpperCase(LOCALE) + text.substring(1);
    }
}
