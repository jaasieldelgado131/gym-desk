package dev.gimnasio.sistemagimnasio.ui.capturas;

import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.ui.GymDeskUi;
import dev.gimnasio.sistemagimnasio.ui.common.AppWindow;
import dev.gimnasio.sistemagimnasio.ui.common.Themes;
import dev.gimnasio.sistemagimnasio.ui.support.FxHarness;
import dev.gimnasio.sistemagimnasio.ui.support.UiFakes;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abre la UI real (con casos de uso sobre repositorios en memoria), guarda capturas en ambos temas a
 * 1200×760 y 1024×680 en {@code target/capturas/} y revisa el orden del foco con el teclado.
 *
 * <p>Necesita pantalla, así que no corre con {@code ./mvnw test}:
 * {@code ./mvnw test -Dtest=CapturasUiTest -Dui.capturas=true}
 */
@EnabledIfSystemProperty(named = "ui.capturas", matches = "true")
class CapturasUiTest {
    private static final Path OUT = Path.of("target", "capturas");
    private static final double[][] SIZES = {{AppWindow.WIDTH, AppWindow.HEIGHT}, {AppWindow.MIN_WIDTH, AppWindow.MIN_HEIGHT}};

    private Stage stage;
    private GymDeskUi ui;
    private Themes themes;

    @BeforeAll
    static void startFx() throws Exception {
        FxHarness.start();
    }

    @AfterEach
    void closeWindow() throws Exception {
        if (stage != null) {
            FxHarness.runFx(() -> {
                ui.close();
                stage.close();
            });
        }
    }

    private Scene open(UiFakes.Users users) throws Exception {
        themes = UiFakes.themes(Themes.Theme.CLARO);
        stage = FxHarness.onFx(Stage::new);
        ui = FxHarness.onFx(() -> {
            GymDeskUi created = new GymDeskUi(stage, () -> UiFakes.useCases(users), themes);
            created.start();
            return created;
        });
        return FxHarness.onFx(stage::getScene);
    }

    /** Captura la pantalla actual en ambos temas y ambos tamaños: {@code <nombre>-<tema>-<ancho>.png}. */
    private List<Path> captureAll(String folder, String name) throws Exception {
        List<Path> files = new ArrayList<>();
        for (Themes.Theme theme : Themes.Theme.values()) {
            FxHarness.runFx(() -> themes.set(theme));
            for (double[] size : SIZES) {
                FxHarness.resizeScene(stage, size[0], size[1]);
                Scene scene = FxHarness.onFx(stage::getScene);
                String file = name + "-" + theme.name().toLowerCase() + "-" + (int) size[0] + ".png";
                files.add(FxHarness.capture(scene, OUT.resolve(folder).resolve(file)));
            }
        }
        FxHarness.runFx(() -> themes.set(Themes.Theme.CLARO));
        FxHarness.resizeScene(stage, AppWindow.WIDTH, AppWindow.HEIGHT);
        return files;
    }

    private List<String> tabOrder(Scene scene, int presses) throws Exception {
        List<String> order = new ArrayList<>();
        order.add(FxHarness.focusOwner(scene));
        for (int i = 0; i < presses; i++) {
            FxHarness.press(scene, KeyCode.TAB, false);
            order.add(FxHarness.focusOwner(scene));
        }
        return order;
    }

    /** Todos los iconos en ambos temas, para revisar que la conversión de Lucide a SVGPath dibuja bien. */
    @Test
    void entrega1Iconos() throws Exception {
        themes = UiFakes.themes(Themes.Theme.CLARO);
        for (Themes.Theme theme : Themes.Theme.values()) {
            Scene scene = FxHarness.onFx(() -> {
                javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane(18, 18);
                grid.setPadding(new javafx.geometry.Insets(24));
                for (String name : dev.gimnasio.sistemagimnasio.ui.common.Icons.names()) {
                    var cell = new javafx.scene.layout.VBox(6, dev.gimnasio.sistemagimnasio.ui.common.Icons.icon(name, 24),
                            new javafx.scene.control.Label(name));
                    cell.setAlignment(javafx.geometry.Pos.CENTER);
                    cell.setPrefWidth(110);
                    grid.getChildren().add(cell);
                }
                grid.getStyleClass().add(theme.styleClass());
                grid.setStyle("-fx-background-color: -gd-superficie;");
                Scene created = new Scene(grid, 720, 420);
                created.getStylesheets().add(AppWindow.stylesheet());
                return created;
            });
            FxHarness.settle();
            FxHarness.capture(scene, OUT.resolve("entrega-1").resolve("iconos-" + theme.name().toLowerCase() + ".png"));
        }
    }

    @Test
    void entrega1Acceso() throws Exception {
        UiFakes.Users users = new UiFakes.Users();
        users.add("Administración", "admin", Role.ADMINISTRADOR, true);
        Scene scene = open(users);
        FxHarness.waitUntil("pantalla de acceso", () -> scene.lookup("#username") != null);
        FxHarness.waitUntil("foco en Usuario", () -> scene.getFocusOwner() != null
                && "username".equals(scene.getFocusOwner().getId()));

        List<Path> files = captureAll("entrega-1", "acceso");

        List<String> order = tabOrder(scene, 4);
        System.out.println("[capturas] " + files);
        System.out.println("[teclado] acceso, Tab: " + order);
        // Los dos botones de tema son un grupo: Tab entra al elegido y las flechas cambian entre ellos.
        assertEquals(List.of("username", "password", "submit", "tema-claro", "username"), order);

        FxHarness.press(scene, KeyCode.TAB, true);
        assertEquals("tema-claro", FxHarness.focusOwner(scene), "Mayús+Tab vuelve al selector de tema");
        FxHarness.press(scene, KeyCode.RIGHT, false);
        assertEquals("tema-oscuro", FxHarness.focusOwner(scene), "flecha derecha pasa al tema nocturno");
        assertEquals(Themes.Theme.OSCURO, FxHarness.onFx(themes::current), "la flecha elige el tema nocturno");
        assertEquals(true, FxHarness.onFx(() -> scene.getRoot().getStyleClass().contains("tema-oscuro")));
        FxHarness.press(scene, KeyCode.SPACE, false);
        assertEquals(Themes.Theme.OSCURO, FxHarness.onFx(themes::current), "Espacio sobre el elegido no lo quita");
        FxHarness.press(scene, KeyCode.LEFT, false);
        assertEquals(Themes.Theme.CLARO, FxHarness.onFx(themes::current), "flecha izquierda vuelve al claro");
        System.out.println("[teclado] acceso: Mayús+Tab → tema-claro; → elige nocturno; Espacio lo mantiene; ← vuelve a claro");
    }
}
