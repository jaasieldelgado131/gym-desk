package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GymSettingsTest {
    private static final Currency MXN = Currency.getInstance("MXN");

    private static GymSettings settings(String name, Currency currency, int warningDays, int graceDays, String prefix) {
        return new GymSettings(name, null, null, null, null, currency, warningDays, graceDays, prefix);
    }

    @Test
    void losValoresIniciales_sonLosDeLaEspecificacion() {
        assertEquals(7, GymSettings.DEFAULT_WARNING_DAYS);
        assertEquals(30, GymSettings.DEFAULT_GRACE_DAYS);
    }

    /** RF-06.2: el plazo de baja automática es obligatorio. */
    @Test
    void plazoDeBaja_debeSerDeAlMenosUnDia() {
        assertEquals(1, settings("Gym", MXN, 7, 1, "GYM-").graceDays());
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, 0, "GYM-"));
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, -30, "GYM-"));
    }

    @Test
    void diasDeAviso_puedenSerCero_peroNoNegativos() {
        assertEquals(0, settings("Gym", MXN, 0, 30, "GYM-").warningDays());
        assertThrows(DomainException.class, () -> settings("Gym", MXN, -1, 30, "GYM-"));
    }

    @Test
    void nombreMonedaYPrefijo_sonObligatorios() {
        assertThrows(DomainException.class, () -> settings(" ", MXN, 7, 30, "GYM-"));
        assertThrows(DomainException.class, () -> settings("Gym", null, 7, 30, "GYM-"));
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, 30, ""));
    }
}
