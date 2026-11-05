package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;

import java.util.Currency;

/**
 * Parámetros propios del gimnasio, definidos por el Administrador.
 * La moneda no puede cambiarse si ya existen cobros o ventas (regla que aplica la capa de aplicación).
 */
public record GymSettings(
        String gymName,
        String address,
        String phone,
        String logoPath,
        String receiptFooter,
        Currency currency,
        int warningDays,
        int graceDays,
        String folioPrefix) {

    public static final int DEFAULT_WARNING_DAYS = 7;
    public static final int DEFAULT_GRACE_DAYS = 30;

    public GymSettings {
        if (gymName == null || gymName.isBlank()) {
            throw new DomainException("El nombre del gimnasio es obligatorio.");
        }
        if (currency == null) {
            throw new DomainException("La moneda es obligatoria.");
        }
        if (warningDays < 0) {
            throw new DomainException("Los días de aviso no pueden ser negativos.");
        }
        if (graceDays < 1) {
            throw new DomainException("El plazo de baja automática es obligatorio y debe ser de al menos 1 día.");
        }
        if (folioPrefix == null || folioPrefix.isBlank()) {
            throw new DomainException("El prefijo de folios es obligatorio.");
        }
    }
}
