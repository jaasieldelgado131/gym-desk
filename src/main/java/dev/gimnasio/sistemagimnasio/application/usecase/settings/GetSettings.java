package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import dev.gimnasio.sistemagimnasio.application.port.out.SettingsRepository;
import dev.gimnasio.sistemagimnasio.application.shared.Authorizer;
import dev.gimnasio.sistemagimnasio.domain.user.Permission;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Objects;

public final class GetSettings {
    private final SettingsRepository settings;
    private final Authorizer authorizer;

    public GetSettings(SettingsRepository settings, Authorizer authorizer) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer");
    }

    public SettingsView execute(User actor) {
        authorizer.require(actor, Permission.MANAGE_SETTINGS);
        return SettingsView.of(settings.get(), settings.countPayments(), settings.countSales(), settings.folioWidth());
    }
}
