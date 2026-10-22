module ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github {
    requires javafx.controls;
    requires javafx.fxml;

    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github to javafx.fxml;
    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador to javafx.fxml;
    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo to javafx.base;

    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github;
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio;
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;
}