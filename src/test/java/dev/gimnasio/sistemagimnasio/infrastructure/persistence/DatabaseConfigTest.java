package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    void sinUrlNiUsuario_usaLosDelDockerComposeLocal() {
        DatabaseConfig config = DatabaseConfig.from(Map.of("DB_PASSWORD", "secreto"));
        assertEquals("jdbc:postgresql://localhost:5432/gimnasio", config.url());
        assertEquals("gimnasio", config.user());
        assertEquals("secreto", config.password());
    }

    @Test
    void lasVariablesDeEntorno_reemplazanLosValoresPorDefecto() {
        DatabaseConfig config = DatabaseConfig.from(Map.of(
                "DB_URL", "jdbc:postgresql://servidor:5432/gym",
                "DB_USER", "caja",
                "DB_PASSWORD", "secreto"));
        assertEquals(new DatabaseConfig("jdbc:postgresql://servidor:5432/gym", "caja", "secreto"), config);
    }

    @Test
    void sinContrasena_fallaIndicandoQueVariableFalta() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> DatabaseConfig.from(Map.of()));
        assertTrue(e.getMessage().contains("DB_PASSWORD"), e.getMessage());
        assertThrows(IllegalStateException.class, () -> DatabaseConfig.from(Map.of("DB_PASSWORD", "  ")));
    }

    @Test
    void laContrasena_noApareceAlImprimirLaConfiguracion() {
        DatabaseConfig config = DatabaseConfig.from(Map.of("DB_PASSWORD", "secreto"));
        assertFalse(config.toString().contains("secreto"), config.toString());
    }
}
