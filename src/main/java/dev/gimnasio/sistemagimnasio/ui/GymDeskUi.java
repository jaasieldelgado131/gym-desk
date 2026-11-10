package dev.gimnasio.sistemagimnasio.ui;

import dev.gimnasio.sistemagimnasio.application.usecase.gyminfo.GymInfo;
import dev.gimnasio.sistemagimnasio.ui.common.AppWindow;
import dev.gimnasio.sistemagimnasio.ui.common.Background;
import dev.gimnasio.sistemagimnasio.ui.common.Formats;
import dev.gimnasio.sistemagimnasio.ui.common.FxmlView;
import dev.gimnasio.sistemagimnasio.ui.common.UseCases;
import dev.gimnasio.sistemagimnasio.ui.startup.StartupController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.Callable;

/** Entrada de la capa de UI: abre la ventana y decide qué pantalla se muestra. */
public final class GymDeskUi implements AutoCloseable {
    private final Stage stage;
    private final Callable<UseCases> connector;
    private AppWindow window;
    private Background background;

    /** @param connector conecta con la base y arma los casos de uso; se llama fuera del hilo de JavaFX */
    public GymDeskUi(Stage stage, Callable<UseCases> connector) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.connector = Objects.requireNonNull(connector, "connector");
    }

    public void start() {
        window = new AppWindow(stage);
        background = Background.start();
        showStartup();
        window.show();
    }

    private void showStartup() {
        var startup = new StartupController(background, connector, this::onConnected, Platform::exit);
        window.setContent(FxmlView.load("startup", startup));
        startup.connect();
    }

    private void onConnected(UseCases useCases, GymInfo gymInfo) {
        window.setGymName(gymInfo.gymName());
        window.setContent(connectedPlaceholder(gymInfo));
    }

    /** Provisional: la fase 1 muestra aquí la pantalla de acceso. */
    private static Parent connectedPlaceholder(GymInfo gymInfo) {
        Label title = new Label(gymInfo.gymName());
        title.getStyleClass().add("title");
        Label today = new Label(Formats.longDate(gymInfo.today()));
        today.getStyleClass().add("muted");
        Label next = new Label("Conectado. La pantalla de acceso llega en la siguiente fase.");
        VBox card = new VBox(8, title, today, next);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        StackPane root = new StackPane(card);
        root.getStyleClass().add("backdrop");
        return root;
    }

    @Override
    public void close() {
        if (background != null) {
            background.close();
        }
    }
}
