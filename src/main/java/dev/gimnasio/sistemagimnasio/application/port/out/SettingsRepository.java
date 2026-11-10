package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

/** Una sola configuración; {@code folioPrefix} y el ancho son los de la serie de cobros. */
public interface SettingsRepository {
    /** @throws IllegalStateException si falta la configuración o la serie de cobros */
    GymSettings get();

    void save(GymSettings settings);

    int folioWidth();

    int countPayments();

    int countSales();
}
