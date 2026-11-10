package dev.gimnasio.sistemagimnasio.application.usecase.gyminfo;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/** Sin actor ni permiso: también lo usa la pantalla de acceso. */
public final class GetGymInfo {
    private final SettingsRepository settings;
    private final Clock clock;

    public GetGymInfo(SettingsRepository settings, Clock clock) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public GymInfo execute() {
        GymSettings current = settings.get();
        return new GymInfo(current.gymName(), current.logo(), current.currency(), LocalDate.now(clock),
                current.warningDays(), current.autoDeactivationDays());
    }
}
