package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app.ContextoApp;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador.SolicitudesControlador;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.*;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudesMemoria;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ServicioSolicitudes;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util.RegistroEventos;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista.ActualizadorPeriodico;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudesFxmlTest {
    @Test void cargaInyeccionYRefrescoSinPerderEdicion() throws Exception {
        var resultado = new CompletableFuture<Void>();
        Platform.startup(() -> { });
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            try {
                var loader = new FXMLLoader(getClass().getResource("app/solicitudes.fxml"));
                Parent vista = loader.load();
                SolicitudesControlador controlador = loader.getController();
                var servicio = new ServicioSolicitudes(new RepositorioSolicitudesMemoria());
                var actualizador = new ActualizadorPeriodico(250);
                controlador.inicializar(new ContextoApp(servicio, new ContadorCompartido(), new RegistroEventos(), null, actualizador));
                servicio.crear("Ana", "Consulta", Prioridad.MEDIA);
                controlador.refrescarTabla();
                TableView<?> tabla = (TableView<?>) vista.lookup("#tblSolicitudes");
                assertEquals(1, tabla.getItems().size());
                tabla.getSelectionModel().select(0);
                TextField nombre = (TextField) vista.lookup("#txtSolicitante");
                assertEquals("Ana", nombre.getText());
                nombre.setText("Edición pendiente");
                actualizador.detener();
                assertEquals("Edición pendiente", nombre.getText());
                assertEquals(0, tabla.getSelectionModel().getSelectedIndex());
                ((Button) vista.lookup("#btnLimpiar")).fire();
                assertTrue(nombre.getText().isEmpty());
                assertTrue(tabla.getSelectionModel().isEmpty());
                resultado.complete(null);
            } catch (Throwable error) { resultado.completeExceptionally(error); }
        });
        try { resultado.get(15, TimeUnit.SECONDS); } finally { Platform.exit(); }
    }
}
