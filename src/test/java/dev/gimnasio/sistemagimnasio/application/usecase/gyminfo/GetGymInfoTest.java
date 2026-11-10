package dev.gimnasio.sistemagimnasio.application.usecase.gyminfo;

import dev.gimnasio.sistemagimnasio.application.fakes.InMemorySettings;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetGymInfoTest {
    private static final ZoneId MEXICO = ZoneId.of("America/Mexico_City");

    private final InMemorySettings settings = new InMemorySettings();

    private GymInfo at(String instant) {
        return new GetGymInfo(settings, Clock.fixed(Instant.parse(instant), MEXICO)).execute();
    }

    @Test
    void devuelveMarcaMonedaYParametros_conLaFechaDelReloj() {
        Currency usd = Currency.getInstance("USD");
        settings.settings = new GymSettings("Gym Fuerza", "Av. 1", "5512345678", new byte[]{1, 2}, "Gracias",
                usd, 10, 45);

        GymInfo info = at("2026-11-09T18:00:00Z");

        assertEquals("Gym Fuerza", info.gymName());
        assertArrayEquals(new byte[]{1, 2}, info.logo());
        assertEquals(usd, info.currency());
        assertEquals(LocalDate.of(2026, 11, 9), info.today());
        assertEquals(10, info.warningDays());
        assertEquals(45, info.autoDeactivationDays());
    }

    @Test
    void hoy_esElDiaEnLaZonaDelReloj_noEnUtc() {
        assertEquals(LocalDate.of(2026, 11, 9), at("2026-11-10T03:00:00Z").today());
    }

    @Test
    void sinLogotipo_devuelveNull() {
        assertNull(at("2026-11-09T18:00:00Z").logo());
    }
}
