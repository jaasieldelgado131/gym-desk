package dev.gimnasio.sistemagimnasio.domain.settings;

import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(ints = {1, 60})
    void diasDeAviso_de1a60_seAceptan(int days) {
        assertEquals(days, settings("Gym", MXN, days, 30, "GYM-").warningDays());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 61})
    void diasDeAviso_fueraDe1a60_seRechazan(int days) {
        assertThrows(DomainException.class, () -> settings("Gym", MXN, days, 30, "GYM-"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 365})
    void plazoDeBaja_de1a365_seAcepta(int days) {
        assertEquals(days, settings("Gym", MXN, 7, days, "GYM-").graceDays());
    }

    @ParameterizedTest
    @ValueSource(ints = {-30, 0, 366})
    void plazoDeBaja_fueraDe1a365_seRechaza(int days) {
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, days, "GYM-"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"G", "GYM-", "GYM-V-", "ABCD1234", "2026-"})
    void prefijo_de1a8MayusculasNumerosOGuiones_seAcepta(String prefix) {
        assertEquals(prefix, settings("Gym", MXN, 7, 30, prefix).folioPrefix());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gym-", "GYM_", "GYM 1", "ABCDE1234", "ÑU-"})
    void prefijo_conOtrosCaracteresOMasDe8_seRechaza(String prefix) {
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, 30, prefix));
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
