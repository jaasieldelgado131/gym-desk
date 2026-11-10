package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.payment.FolioSeries;

public interface FolioSeriesRepository {
    /** @throws IllegalStateException si la serie no existe */
    FolioSeries get(String series);

    /** Guarda prefijo y ancho; el contador no cambia. */
    void save(FolioSeries series);
}
