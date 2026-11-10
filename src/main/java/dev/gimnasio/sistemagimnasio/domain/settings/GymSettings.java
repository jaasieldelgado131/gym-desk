package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.Currency;

/**
 * Que la moneda no cambie si ya hay cobros o ventas lo valida la capa de aplicación.
 * {@code logo} es la imagen guardada, o null; se copia para que nadie la altere desde fuera.
 * El prefijo de folios vive en su serie ({@code FolioSeries}).
 */
public record GymSettings(
        String gymName,
        String address,
        String phone,
        byte[] logo,
        String receiptFooter,
        Currency currency,
        int warningDays,
        int autoDeactivationDays) {

    public static final int DEFAULT_WARNING_DAYS = 7;
    public static final int DEFAULT_AUTO_DEACTIVATION_DAYS = 30;
    public static final int MAX_WARNING_DAYS = 60;
    public static final int MAX_AUTO_DEACTIVATION_DAYS = 365;

    public GymSettings {
        if (gymName == null || gymName.isBlank()) {
            throw new DomainException("El nombre del gimnasio es obligatorio.");
        }
        if (currency == null) {
            throw new DomainException("La moneda es obligatoria.");
        }
        if (warningDays < 1 || warningDays > MAX_WARNING_DAYS) {
            throw new DomainException("Los días de aviso deben estar entre 1 y 60.");
        }
        if (autoDeactivationDays < 1 || autoDeactivationDays > MAX_AUTO_DEACTIVATION_DAYS) {
            throw new DomainException("El plazo de baja automática debe estar entre 1 y 365 días.");
        }
        logo = logo == null ? null : logo.clone();
    }

    @Override
    public byte[] logo() {
        return logo == null ? null : logo.clone();
    }
}
