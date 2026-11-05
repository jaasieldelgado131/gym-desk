module dev.gimnasio.sistemagimnasio {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.gimnasio.sistemagimnasio to javafx.fxml;
    exports dev.gimnasio.sistemagimnasio;
}