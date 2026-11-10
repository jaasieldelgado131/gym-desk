package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.Currency;
import java.util.regex.Pattern;

/**
 * Que la moneda no cambie si ya hay cobros o ventas lo valida la capa de aplicación.
 * {@code logo} es la imagen guardada, o null; se copia para que nadie la altere desde fuera.
 */
public record GymSettings(
        String gymName,
        String address,
        String phone,
        byte[] logo,
        String receiptFooter,
        Currency currency,
        int warningDays,
        int graceDays,
        String folioPrefix) {

    public static final int DEFAULT_WARNING_DAYS = 7;
    public static final int DEFAULT_GRACE_DAYS = 30;
    public static final int MAX_WARNING_DAYS = 60;
    public static final int MAX_GRACE_DAYS = 365;
    private static final Pattern FOLIO_PREFIX = Pattern.compile("[A-Z0-9-]{1,8}");

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
        if (graceDays < 1 || graceDays > MAX_GRACE_DAYS) {
            throw new DomainException("El plazo de baja automática debe estar entre 1 y 365 días.");
        }
        if (folioPrefix == null || !FOLIO_PREFIX.matcher(folioPrefix).matches()) {
            throw new DomainException("El prefijo de folios debe tener de 1 a 8 letras mayúsculas, números o guiones.");
        }
        logo = logo == null ? null : logo.clone();
    }

    @Override
    public byte[] logo() {
        return logo == null ? null : logo.clone();
    }
}
