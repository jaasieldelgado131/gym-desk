package dev.gimnasio.sistemagimnasio.ui;

import dev.gimnasio.sistemagimnasio.application.usecase.gyminfo.GymInfo;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import dev.gimnasio.sistemagimnasio.ui.common.AppWindow;
import dev.gimnasio.sistemagimnasio.ui.common.Background;
import dev.gimnasio.sistemagimnasio.ui.common.FxmlView;
import dev.gimnasio.sistemagimnasio.ui.common.Session;
import dev.gimnasio.sistemagimnasio.ui.common.Themes;
import dev.gimnasio.sistemagimnasio.ui.common.UseCases;
import dev.gimnasio.sistemagimnasio.ui.login.LoginController;
import dev.gimnasio.sistemagimnasio.ui.login.LoginViewModel;
import dev.gimnasio.sistemagimnasio.ui.startup.StartupController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
    private final Themes themes;
    private final Session session = new Session();
    private AppWindow window;
    private UseCases useCases;
    private GymInfo gymInfo;
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

    public Session session() {
        return session;
    }

    private void showStartup() {
        var startup = new StartupController(background, connector, this::onConnected, Platform::exit);
        window.setContent(FxmlView.load("startup", startup));
        startup.connect();
    }

    private void onConnected(UseCases connected, GymInfo info) {
        this.useCases = connected;
        this.gymInfo = info;
        window.setGymName(info.gymName());
        showLogin();
    }

    private void showLogin() {
        var model = new LoginViewModel(useCases.authenticate(), background, this::onSignedIn);
        window.setContent(FxmlView.load("login", new LoginController(gymInfo, themes, model)));
    }

    private void onSignedIn(User user) {
        session.open(user);
        window.setContent(signedInPlaceholder(user));
    }

    /** Cerrar sesión no es un caso de uso: se descarta el actor y se vuelve al acceso. */
    private void signOut() {
        session.close();
        showLogin();
    }

    /** Provisional: la entrega 3 muestra aquí el menú y la cabecera. */
    private Parent signedInPlaceholder(User user) {
        Label title = new Label("Sesión iniciada");
        title.getStyleClass().add("titulo-24");
        Label who = new Label(user.name() + " · " + user.role().displayName() + " · " + user.username());
        who.getStyleClass().add("texto-2");
        who.setId("sesion-usuario");
        Label next = new Label("El menú y la cabecera llegan en la siguiente entrega.");
        next.getStyleClass().add("texto-3");
        Button logout = new Button("Cerrar sesión");
        logout.setId("cerrar-sesion");
        logout.setOnAction(e -> signOut());
        VBox card = new VBox(10, title, who, next, logout);
        card.getStyleClass().addAll("marco-contenido", "acceso-form");
        card.setAlignment(Pos.CENTER_LEFT);
        StackPane frame = new StackPane(card);
        frame.getStyleClass().add("marco");
        frame.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        StackPane root = new StackPane(frame);
        root.getStyleClass().addAll("fondo-app", "acceso");
        return root;
    }

    @Override
    public void close() {
        if (background != null) {
            background.close();
        }
    }
}
