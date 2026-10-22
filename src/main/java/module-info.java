module ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github {
    requires javafx.controls;
    requires javafx.fxml;

    // 1. Exportar el paquete donde está tu clase Main para que JavaFX pueda arrancar la app
    exports ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app;

    // 2. Abrir el paquete de los controladores a javafx.fxml para que funcione el @FXML
    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador to javafx.fxml;

    // (Opcional pero recomendado) Abrir el paquete app por si el FXMLLoader necesita acceso allí
    opens ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app to javafx.fxml;
}