package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

/** Una sola configuración por gimnasio. */
public interface SettingsRepository {
    /** @throws IllegalStateException si falta la configuración */
    GymSettings get();

    void save(GymSettings settings);
}
