package dev.gimnasio.sistemagimnasio.ui.common;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

/** La ventana principal: una sola escena con la hoja de estilos de la app, cuyo contenido se reemplaza. */
public final class AppWindow {
    static final String APP_NAME = "Sistema Gimnasio";
    private static final String STYLESHEET = "/css/app.css";
    private static final double WIDTH = 1200;
    private static final double HEIGHT = 760;
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 680;

    private final Stage stage;
    private final Scene scene;

    public AppWindow(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.scene = new Scene(new StackPane(), WIDTH, HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(
                AppWindow.class.getResource(STYLESHEET), "Falta " + STYLESHEET).toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setTitle(APP_NAME);
    }

    public void show() {
        stage.show();
    }

    public void setContent(Parent content) {
        scene.setRoot(content);
    }

    /** «Mi gimnasio · Sistema Gimnasio». */
    public void setGymName(String gymName) {
        stage.setTitle(gymName == null || gymName.isBlank() ? APP_NAME : gymName + " · " + APP_NAME);
    }

    public Window window() {
        return stage;
    }
}
