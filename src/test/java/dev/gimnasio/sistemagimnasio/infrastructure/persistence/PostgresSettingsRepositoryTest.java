package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
class PostgresSettingsRepositoryTest {
    private static final Currency MXN = Currency.getInstance("MXN");

    private Jdbi jdbi;
    private PostgresSettingsRepository repository;

    @BeforeEach
    void setUp() {
        jdbi = TestDatabase.jdbi();
        TestDatabase.clean();
        repository = new PostgresSettingsRepository(jdbi);
    }

    private void insertDefaults() {
        jdbi.useHandle(handle -> handle.execute("INSERT INTO configuracion (nombre_gimnasio) VALUES ('Mi gimnasio')"));
    }

    private static GymSettings plain(byte[] logo) {
        return new GymSettings("Gym", null, null, logo, null, MXN, 7, 30);
    }

    @Test
    void lee_losValoresPorDefectoDeLaBase() {
        insertDefaults();

        GymSettings settings = repository.get();

        assertEquals("Mi gimnasio", settings.gymName());
        assertNull(settings.address());
        assertNull(settings.phone());
        assertNull(settings.logo());
        assertNull(settings.receiptFooter());
        assertEquals(MXN, settings.currency());
        assertEquals(7, settings.warningDays());
        assertEquals(30, settings.autoDeactivationDays());
    }

    @Test
    void guarda_todosLosCampos_incluidoElLogotipo() {
        insertDefaults();
        byte[] logo = {(byte) 0x89, 0x50, 0x4E, 0x47, 0, 1};

        repository.save(new GymSettings("Gym Fuerza", "Av. Juárez 10", "55 1234 5678", logo, "¡Gracias!",
                Currency.getInstance("USD"), 10, 45));
        GymSettings read = repository.get();

        assertEquals("Gym Fuerza", read.gymName());
        assertEquals("Av. Juárez 10", read.address());
        assertEquals("55 1234 5678", read.phone());
        assertArrayEquals(logo, read.logo());
        assertEquals("¡Gracias!", read.receiptFooter());
        assertEquals(Currency.getInstance("USD"), read.currency());
        assertEquals(10, read.warningDays());
        assertEquals(45, read.autoDeactivationDays());
    }

    @Test
    void quitarElLogotipo_loDejaEnNull() {
        insertDefaults();
        repository.save(plain(new byte[]{1}));

        repository.save(plain(null));

        assertNull(repository.get().logo());
    }

    @Test
    void sinConfiguracion_fallaConClaridad() {
        assertThrows(IllegalStateException.class, repository::get);
        assertThrows(IllegalStateException.class, () -> repository.save(plain(null)));
    }
}
