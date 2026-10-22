module ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github to javafx.fxml;
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github;
}