package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GymSettingsTest {
    private static final Currency MXN = Currency.getInstance("MXN");

    private static GymSettings settings(String name, Currency currency, int warningDays, int graceDays, String prefix) {
        return new GymSettings(name, null, null, null, null, currency, warningDays, graceDays, prefix);
    }

    @Test
    void valoresIniciales_sieteDiasDeAvisoYTreintaDePlazo() {
        assertEquals(7, GymSettings.DEFAULT_WARNING_DAYS);
        assertEquals(30, GymSettings.DEFAULT_GRACE_DAYS);
    }

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
    void elLogotipo_esOpcional_yNoSePuedeAlterarDesdeFuera() {
        assertNull(settings("Gym", MXN, 7, 30, "GYM-").logo());

        byte[] image = {1, 2, 3};
        GymSettings withLogo = new GymSettings("Gym", null, null, image, null, MXN, 7, 30, "GYM-");
        image[0] = 9;
        withLogo.logo()[1] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, withLogo.logo());
    }

    @Test
    void nombreMonedaYPrefijo_sonObligatorios() {
        assertThrows(DomainException.class, () -> settings(" ", MXN, 7, 30, "GYM-"));
        assertThrows(DomainException.class, () -> settings("Gym", null, 7, 30, "GYM-"));
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, 30, ""));
    }
}
