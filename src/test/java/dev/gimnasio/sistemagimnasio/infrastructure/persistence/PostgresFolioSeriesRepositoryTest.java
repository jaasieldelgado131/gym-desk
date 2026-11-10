package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.domain.payment.FolioSeries;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
class PostgresFolioSeriesRepositoryTest {
    private Jdbi jdbi;
    private PostgresFolioSeriesRepository repository;

    @BeforeEach
    void setUp() {
        jdbi = TestDatabase.jdbi();
        TestDatabase.clean();
        repository = new PostgresFolioSeriesRepository(jdbi);
    }

    private void insertSeries() {
        jdbi.useHandle(handle -> handle.execute(
                "INSERT INTO folio_secuencia (serie, prefijo, ultimo) VALUES ('COBRO', 'GYM-', 41)"));
    }

    private long last() {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT ultimo FROM folio_secuencia WHERE serie = 'COBRO'")
                .mapTo(Long.class).one());
    }

    @Test
    void lee_prefijoYAnchoPorDefecto() {
        insertSeries();
        assertEquals(new FolioSeries("COBRO", "GYM-", 4), repository.get("COBRO"));
    }

    @Test
    void guarda_prefijoYAncho_sinTocarElContador() {
        insertSeries();

        repository.save(new FolioSeries("COBRO", "GF-", 6));

        assertEquals(new FolioSeries("COBRO", "GF-", 6), repository.get("COBRO"));
        assertEquals(41, last());
    }

    @Test
    void serieInexistente_fallaConClaridad() {
        assertThrows(IllegalStateException.class, () -> repository.get("COBRO"));
        assertThrows(IllegalStateException.class, () -> repository.save(new FolioSeries("COBRO", "GYM-", 4)));
    }
}
