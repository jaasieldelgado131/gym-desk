package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** En una base propia: las demás pruebas vacían las tablas de la base compartida. */
@Testcontainers(disabledWithoutDocker = true)
class InitialDataTest {
    private static HikariDataSource dataSource;
    private static Jdbi jdbi;

    @BeforeAll
    static void migrateEmptyDatabase() {
        dataSource = Database.connect(TestDatabase.newEmptyDatabase("datos_iniciales"));
        Database.migrate(dataSource);
        jdbi = Jdbi.create(dataSource);
    }

    @AfterAll
    static void close() {
        dataSource.close();
    }

    @Test
    void configuracionInicial() {
        Map<String, Object> row = jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT nombre_gimnasio, moneda, dias_aviso, dias_baja_automatica, logotipo
                        FROM configuracion""").mapToMap().one());

        assertEquals("Mi gimnasio", row.get("nombre_gimnasio"));
        assertEquals("MXN", row.get("moneda"));
        assertEquals(7, row.get("dias_aviso"));
        assertEquals(30, row.get("dias_baja_automatica"));
        assertEquals(null, row.get("logotipo"));
    }

    @Test
    void serieDeCobrosInicial() {
        Map<String, Object> row = jdbi.withHandle(handle -> handle.createQuery(
                "SELECT serie, prefijo, ultimo, ancho FROM folio_secuencia").mapToMap().one());

        assertEquals("COBRO", row.get("serie"));
        assertEquals("GYM-", row.get("prefijo"));
        assertEquals(0L, row.get("ultimo"));
        assertEquals(4, ((Number) row.get("ancho")).intValue());
    }

    @Test
    void metodosDePagoIniciales_soloEfectivoPideLoRecibido() {
        List<String> rows = jdbi.withHandle(handle -> handle.createQuery(
                        "SELECT nombre || ':' || es_efectivo || ':' || activo FROM metodo_pago ORDER BY id")
                .mapTo(String.class).list());

        assertEquals(List.of("Efectivo:true:true", "Tarjeta:false:true", "Transferencia:false:true"), rows);
    }
}
