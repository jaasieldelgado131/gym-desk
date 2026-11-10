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

    private static GymSettings settings(String name, Currency currency, int warningDays, int autoDeactivationDays) {
        return new GymSettings(name, null, null, null, null, currency, warningDays, autoDeactivationDays);
    }

    @Test
    void valoresIniciales_sieteDiasDeAvisoYTreintaDePlazo() {
        assertEquals(7, GymSettings.DEFAULT_WARNING_DAYS);
        assertEquals(30, GymSettings.DEFAULT_AUTO_DEACTIVATION_DAYS);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 60})
    void diasDeAviso_de1a60_seAceptan(int days) {
        assertEquals(days, settings("Gym", MXN, days, 30).warningDays());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 61})
    void diasDeAviso_fueraDe1a60_seRechazan(int days) {
        assertThrows(DomainException.class, () -> settings("Gym", MXN, days, 30));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 365})
    void plazoDeBaja_de1a365_seAcepta(int days) {
        assertEquals(days, settings("Gym", MXN, 7, days).autoDeactivationDays());
    }

    @ParameterizedTest
    @ValueSource(ints = {-30, 0, 366})
    void plazoDeBaja_fueraDe1a365_seRechaza(int days) {
        assertThrows(DomainException.class, () -> settings("Gym", MXN, 7, days));
    }

    @Test
    void elLogotipo_esOpcional_yNoSePuedeAlterarDesdeFuera() {
        assertNull(settings("Gym", MXN, 7, 30).logo());

        byte[] image = {1, 2, 3};
        GymSettings withLogo = new GymSettings("Gym", null, null, image, null, MXN, 7, 30);
        image[0] = 9;
        withLogo.logo()[1] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, withLogo.logo());
    }

    @Test
    void losTextos_respetanSuLargoMaximo() {
        assertEquals(100, settings("x".repeat(GymSettings.MAX_GYM_NAME_LENGTH), MXN, 7, 30).gymName().length());
        assertThrows(DomainException.class,
                () -> settings("x".repeat(GymSettings.MAX_GYM_NAME_LENGTH + 1), MXN, 7, 30));
        assertThrows(DomainException.class, () -> new GymSettings("Gym", "x".repeat(GymSettings.MAX_ADDRESS_LENGTH + 1),
                null, null, null, MXN, 7, 30));
        assertThrows(DomainException.class, () -> new GymSettings("Gym", null, "x".repeat(GymSettings.MAX_PHONE_LENGTH + 1),
                null, null, MXN, 7, 30));
        assertThrows(DomainException.class, () -> new GymSettings("Gym", null, null, null,
                "x".repeat(GymSettings.MAX_RECEIPT_FOOTER_LENGTH + 1), MXN, 7, 30));
    }

    @Test
    void elLogotipo_respetaSuPesoMaximo() {
        new GymSettings("Gym", null, null, new byte[GymSettings.MAX_LOGO_BYTES], null, MXN, 7, 30);
        assertThrows(DomainException.class,
                () -> new GymSettings("Gym", null, null, new byte[GymSettings.MAX_LOGO_BYTES + 1], null, MXN, 7, 30));
    }

    @Test
    void losMensajes_usanLosLimitesVigentes() {
        DomainException e = assertThrows(DomainException.class,
                () -> settings("Gym", MXN, GymSettings.MAX_WARNING_DAYS + 1, 30));
        assertEquals("Los días de aviso deben estar entre 1 y " + GymSettings.MAX_WARNING_DAYS + ".", e.getMessage());
    }

    @Test
    void nombreYMoneda_sonObligatorios() {
        assertThrows(DomainException.class, () -> settings(" ", MXN, 7, 30));
        assertThrows(DomainException.class, () -> settings("Gym", null, 7, 30));
    }
}
