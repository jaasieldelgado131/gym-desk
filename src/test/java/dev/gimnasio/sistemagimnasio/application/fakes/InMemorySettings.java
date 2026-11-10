package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

import java.util.Currency;

public final class InMemorySettings implements SettingsRepository {
    public static final Currency MXN = Currency.getInstance("MXN");

    public GymSettings settings = new GymSettings("Mi gimnasio", null, null, null, null, MXN, 7, 30);
    public int saves;

    @Override
    public GymSettings get() {
        return settings;
    }

    @Override
    public void save(GymSettings settings) {
        this.settings = settings;
        saves++;
    }
}
