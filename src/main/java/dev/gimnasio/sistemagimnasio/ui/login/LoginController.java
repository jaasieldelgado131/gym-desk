package dev.gimnasio.sistemagimnasio.ui.login;

import dev.gimnasio.sistemagimnasio.application.usecase.gyminfo.GymInfo;
import dev.gimnasio.sistemagimnasio.ui.common.BrandLogo;
import dev.gimnasio.sistemagimnasio.ui.common.FieldErrors;
import dev.gimnasio.sistemagimnasio.ui.common.Icons;
import dev.gimnasio.sistemagimnasio.ui.common.ThemeSelector;
import dev.gimnasio.sistemagimnasio.ui.common.Themes;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.Objects;

/** Pantalla de acceso. */
public final class LoginController {
    private final GymInfo gymInfo;
    private final Themes themes;
    private final FieldErrors fieldErrors = new FieldErrors();

    @FXML
    private HBox brand;
    @FXML
    private Label gymName;
    @FXML
    private HBox errorNote;
    @FXML
    private Label errorText;
    @FXML
    private TextField username;
    @FXML
    private Label usernameError;
    @FXML
    private PasswordField password;
    @FXML
    private Label passwordError;
    @FXML
    private Button submit;
    @FXML
    private HBox themeRow;

    public LoginController(GymInfo gymInfo, Themes themes) {
        this.gymInfo = Objects.requireNonNull(gymInfo, "gymInfo");
        this.themes = Objects.requireNonNull(themes, "themes");
    }

    @FXML
    private void initialize() {
        brand.getChildren().addFirst(BrandLogo.create(gymInfo.gymName(), gymInfo.logo(), 48));
        gymName.setText(gymInfo.gymName());
        errorNote.getChildren().addFirst(Icons.icon("alerta"));
        errorNote.managedProperty().bind(errorNote.visibleProperty());
        errorNote.setVisible(false);
        fieldErrors.bind("username", username, usernameError).bind("password", password, passwordError);
        themeRow.getChildren().add(ThemeSelector.create(themes));
        Platform.runLater(username::requestFocus);
    }

    @FXML
    private void submit() {
        // Se conecta con Authenticate en la siguiente entrega.
    }
}
