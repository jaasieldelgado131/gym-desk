package dev.gimnasio.sistemagimnasio.ui.common;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/** Diálogos modales con los textos y botones de la app. */
public final class Dialogs {
    private Dialogs() {
    }

    public static void error(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, new ButtonType("Entendido", ButtonBar.ButtonData.OK_DONE));
        prepare(alert, owner, "No se pudo completar");
        alert.showAndWait();
    }

    /** @return {@code true} si se eligió {@code confirmText} */
    public static boolean confirm(Window owner, String title, String message, String confirmText) {
        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, cancel, confirm);
        prepare(alert, owner, title);
        return alert.showAndWait().filter(confirm::equals).isPresent();
    }

    private static void prepare(Alert alert, Window owner, String title) {
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        // El tema lo pone Themes al abrirse la ventana del diálogo.
        alert.getDialogPane().getStylesheets().add(AppWindow.stylesheet());
    }
}
