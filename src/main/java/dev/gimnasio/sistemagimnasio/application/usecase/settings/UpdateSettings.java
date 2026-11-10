package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import dev.gimnasio.sistemagimnasio.application.port.out.FolioSeriesRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.MembershipPaymentRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.SaleRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.application.port.out.TransactionRunner;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.application.shared.ValidationException;
import dev.gimnasio.sistemagimnasio.domain.payment.FolioSeries;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Revisar el bloqueo de moneda y guardar ocurren en la misma transacción que la configuración y la serie. */
public final class UpdateSettings {
    static final String GYM_NAME_REQUIRED = "Escribe el nombre del gimnasio.";
    static final String WARNING_DAYS_RANGE = "Escribe un número de días entre 1 y 60.";
    static final String AUTO_DEACTIVATION_DAYS_RANGE = "Escribe un número de días entre 1 y 365.";
    static final String CURRENCY_REQUIRED = "Elige una moneda.";
    static final String FOLIO_PREFIX_FORMAT = "Usa de 1 a 8 letras mayúsculas, números o guiones, por ejemplo GYM-.";
    static final String LOGO_FORMAT = "Usa una imagen PNG, JPG, WEBP o SVG de hasta 300 KB.";

    private static final int GYM_NAME_MAX = 100;
    private static final int ADDRESS_MAX = 200;
    private static final int PHONE_MAX = 30;
    private static final int RECEIPT_FOOTER_MAX = 160;

    private final SettingsRepository settings;
    private final FolioSeriesRepository folioSeries;
    private final MembershipPaymentRepository payments;
    private final SaleRepository sales;
    private final TransactionRunner transactions;
    private final Authorizer authorizer;

    public UpdateSettings(SettingsRepository settings, FolioSeriesRepository folioSeries,
                          MembershipPaymentRepository payments, SaleRepository sales,
                          TransactionRunner transactions, Authorizer authorizer) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.folioSeries = Objects.requireNonNull(folioSeries, "folioSeries");
        this.payments = Objects.requireNonNull(payments, "payments");
        this.sales = Objects.requireNonNull(sales, "sales");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public SettingsView execute(User actor, UpdateSettingsCommand command) {
        return transactions.inTransaction(() -> {
            authorizer.require(actor, Permission.MANAGE_SETTINGS);
            GymSettings current = settings.get();
            FolioSeries series = folioSeries.get(FolioSeries.MEMBERSHIP_PAYMENTS);
            RecordedOperations operations = RecordedOperations.count(payments, sales);

            String gymName = strip(command.gymName());
            String address = strip(command.address());
            String phone = strip(command.phone());
            String receiptFooter = strip(command.receiptFooter());
            String folioPrefix = strip(command.folioPrefix());
            Currency currency = command.currency();

            Map<String, String> errors = new LinkedHashMap<>();
            if (gymName == null) {
                errors.put("gymName", GYM_NAME_REQUIRED);
            } else {
                checkLength(errors, "gymName", gymName, GYM_NAME_MAX);
            }
            checkLength(errors, "address", address, ADDRESS_MAX);
            checkLength(errors, "phone", phone, PHONE_MAX);
            checkLength(errors, "receiptFooter", receiptFooter, RECEIPT_FOOTER_MAX);
            if (command.logo() != null && !LogoFormat.isAccepted(command.logo())) {
                errors.put("logo", LOGO_FORMAT);
            }
            if (currency == null) {
                errors.put("currency", CURRENCY_REQUIRED);
            } else if (operations.locksCurrency() && !currency.equals(current.currency())) {
                errors.put("currency", "No se puede cambiar: ya hay cobros o ventas registrados en "
                        + current.currency().getCurrencyCode() + ".");
            }
            if (!inRange(command.warningDays(), GymSettings.MAX_WARNING_DAYS)) {
                errors.put("warningDays", WARNING_DAYS_RANGE);
            }
            if (!inRange(command.autoDeactivationDays(), GymSettings.MAX_AUTO_DEACTIVATION_DAYS)) {
                errors.put("autoDeactivationDays", AUTO_DEACTIVATION_DAYS_RANGE);
            }
            if (!FolioSeries.isValidPrefix(folioPrefix)) {
                errors.put("folioPrefix", FOLIO_PREFIX_FORMAT);
            }
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }

            GymSettings updated = new GymSettings(gymName, address, phone, command.logo(), receiptFooter, currency,
                    command.warningDays(), command.autoDeactivationDays());
            FolioSeries updatedSeries = series.withPrefix(folioPrefix);
            settings.save(updated);
            folioSeries.save(updatedSeries);
            return SettingsView.of(updated, updatedSeries, operations);
        });
    }

    /** Recorta los extremos; en blanco cuenta como no capturado. */
    private static String strip(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    private static void checkLength(Map<String, String> errors, String field, String value, int max) {
        if (value != null && value.length() > max) {
            errors.put(field, "Usa " + max + " caracteres o menos.");
        }
    }

    private static boolean inRange(Integer value, int max) {
        return value != null && value >= 1 && value <= max;
    }
}
