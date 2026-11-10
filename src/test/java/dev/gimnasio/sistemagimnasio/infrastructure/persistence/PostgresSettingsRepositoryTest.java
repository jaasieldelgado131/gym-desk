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

    private void execute(String sql) {
        jdbi.useHandle(handle -> handle.execute(sql));
    }

    private long id(String insert) {
        return jdbi.withHandle(handle -> handle.createQuery(insert + " RETURNING id").mapTo(Long.class).one());
    }

    private void insertDefaults() {
        execute("INSERT INTO configuracion (nombre_gimnasio) VALUES ('Mi gimnasio')");
        execute("INSERT INTO folio_secuencia (serie, prefijo) VALUES ('COBRO', 'GYM-')");
    }

    private static GymSettings plain(byte[] logo) {
        return new GymSettings("Gym", null, null, logo, null, MXN, 7, 30, "GYM-");
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
        assertEquals(30, settings.graceDays());
        assertEquals("GYM-", settings.folioPrefix());
        assertEquals(4, repository.folioWidth());
    }

    @Test
    void guarda_todosLosCampos_incluidoElLogotipoYElPrefijoDeCobros() {
        insertDefaults();
        byte[] logo = {(byte) 0x89, 0x50, 0x4E, 0x47, 0, 1};

        repository.save(new GymSettings("Gym Fuerza", "Av. Juárez 10", "55 1234 5678", logo, "¡Gracias!",
                Currency.getInstance("USD"), 10, 45, "GF-"));
        GymSettings read = repository.get();

        assertEquals("Gym Fuerza", read.gymName());
        assertEquals("Av. Juárez 10", read.address());
        assertEquals("55 1234 5678", read.phone());
        assertArrayEquals(logo, read.logo());
        assertEquals("¡Gracias!", read.receiptFooter());
        assertEquals(Currency.getInstance("USD"), read.currency());
        assertEquals(10, read.warningDays());
        assertEquals(45, read.graceDays());
        assertEquals("GF-", read.folioPrefix());
    }

    @Test
    void quitarElLogotipo_loDejaEnNull() {
        insertDefaults();
        repository.save(plain(new byte[]{1}));

        repository.save(plain(null));

        assertNull(repository.get().logo());
    }

    @Test
    void sinConfiguracion_oSinSerieDeCobros_fallaConClaridad() {
        assertThrows(IllegalStateException.class, repository::get);
        execute("INSERT INTO configuracion (nombre_gimnasio) VALUES ('Mi gimnasio')");
        assertThrows(IllegalStateException.class, repository::get);
        assertThrows(IllegalStateException.class, repository::folioWidth);
        assertThrows(IllegalStateException.class, () -> repository.save(plain(null)));
    }

    @Test
    void cuentaCobrosYVentas() {
        insertDefaults();
        assertEquals(0, repository.countPayments());
        assertEquals(0, repository.countSales());

        long usuario = id("INSERT INTO usuario (nombre, nombre_usuario, contrasena_hash, rol) VALUES ('Ana', 'ana', 'h', 'caja')");
        long socio = id("INSERT INTO socio (nombre, telefono, fecha_alta) VALUES ('Luis', '5512345678', '2026-11-01')");
        long metodo = id("INSERT INTO metodo_pago (nombre) VALUES ('Tarjeta')");
        for (int i = 1; i <= 2; i++) {
            execute("INSERT INTO pago (folio, socio_id, usuario_id, metodo_pago_id, monto) VALUES ('GYM-" + i + "', "
                    + socio + ", " + usuario + ", " + metodo + ", 450)");
        }
        execute("INSERT INTO venta (folio, metodo_pago_id, total, usuario_id) VALUES ('GYM-V-1', "
                + metodo + ", 18.50, " + usuario + ")");

        assertEquals(2, repository.countPayments());
        assertEquals(1, repository.countSales());
    }
}
