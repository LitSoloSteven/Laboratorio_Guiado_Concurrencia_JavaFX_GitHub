package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrincipalControlador {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
