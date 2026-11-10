package dev.gimnasio.sistemagimnasio.ui;

import dev.gimnasio.sistemagimnasio.application.usecase.gyminfo.GymInfo;
import dev.gimnasio.sistemagimnasio.ui.common.AppWindow;
import dev.gimnasio.sistemagimnasio.ui.common.Background;
import dev.gimnasio.sistemagimnasio.ui.common.FxmlView;
import dev.gimnasio.sistemagimnasio.ui.common.Themes;
import dev.gimnasio.sistemagimnasio.ui.common.UseCases;
import dev.gimnasio.sistemagimnasio.ui.login.LoginController;
import dev.gimnasio.sistemagimnasio.ui.startup.StartupController;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.Callable;

/** Entrada de la capa de UI: abre la ventana y decide qué pantalla se muestra. */
public final class GymDeskUi implements AutoCloseable {
    private final Stage stage;
    private final Callable<UseCases> connector;
    private final Themes themes;
    private AppWindow window;
    private Background background;

    /** @param connector conecta con la base y arma los casos de uso; se llama fuera del hilo de JavaFX */
    public GymDeskUi(Stage stage, Callable<UseCases> connector) {
        this(stage, connector, Themes.fromUserPreferences());
    }

    public GymDeskUi(Stage stage, Callable<UseCases> connector, Themes themes) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.connector = Objects.requireNonNull(connector, "connector");
        this.themes = Objects.requireNonNull(themes, "themes");
    }

    public void start() {
        window = new AppWindow(stage, themes);
        themes.installOnNewWindows();
        background = Background.start();
        showStartup();
        window.show();
    }

    public AppWindow window() {
        return window;
    }

    private void showStartup() {
        var startup = new StartupController(background, connector, this::onConnected, Platform::exit);
        window.setContent(FxmlView.load("startup", startup));
        startup.connect();
    }

    private void onConnected(UseCases useCases, GymInfo gymInfo) {
        window.setGymName(gymInfo.gymName());
        showLogin(gymInfo);
    }

    private void showLogin(GymInfo gymInfo) {
        window.setContent(FxmlView.load("login", new LoginController(gymInfo, themes)));
    }

    @Override
    public void close() {
        if (background != null) {
            background.close();
        }
    }
}
