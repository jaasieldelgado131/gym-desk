package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

import java.util.Currency;

public final class InMemorySettings implements SettingsRepository {
    public static final Currency MXN = Currency.getInstance("MXN");

    public GymSettings settings = new GymSettings("Mi gimnasio", null, null, null, null, MXN, 7, 30, "GYM-");
    public int folioWidth = 4;
    public int payments;
    public int sales;
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

    @Override
    public int folioWidth() {
        return folioWidth;
    }

    @Override
    public int countPayments() {
        return payments;
    }

    @Override
    public int countSales() {
        return sales;
    }
}
