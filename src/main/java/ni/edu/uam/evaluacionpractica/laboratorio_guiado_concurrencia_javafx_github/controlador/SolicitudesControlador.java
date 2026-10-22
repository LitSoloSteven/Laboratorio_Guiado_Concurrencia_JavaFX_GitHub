package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app.ContextoApp;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ReglaNegocioException;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ServicioSolicitudes;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la vista del módulo CRUD de solicitudes (solicitudes.fxml).
 * Gestiona la interacción del usuario: creación, edición, eliminación y listado.
 */
public class SolicitudesControlador {

    // Componentes del formulario FXML
    @FXML private TextField txtSolicitante;
    @FXML private TextField txtAsunto;
    @FXML private ComboBox<Prioridad> cmbPrioridad;

    // Botones de acción
    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCargarEjemplos;

    // Tabla de visualización y sus columnas
    @FXML private TableView<Solicitud> tblSolicitudes;
    @FXML private TableColumn<Solicitud, Number> colId;
    @FXML private TableColumn<Solicitud, String> colSolicitante;
    @FXML private TableColumn<Solicitud, String> colAsunto;
    @FXML private TableColumn<Solicitud, String> colPrioridad;
    @FXML private TableColumn<Solicitud, String> colEstado;
    @FXML private TableColumn<Solicitud, String> colAtendidaPor;

    // Etiqueta para resumen cuantitativo
    @FXML private Label lblResumen;

    // Servicio encargado de las reglas de negocio y almacenamiento
    private ServicioSolicitudes servicio = new ServicioSolicitudes();

    /**
     * Inicialización del controlador invocado por JavaFX tras cargar el archivo FXML.
     */
    @FXML
    public void initialize() {
        // 1. Cargar las prioridades disponibles en el ComboBox
        cmbPrioridad.getItems().setAll(Prioridad.values());

        // 2. Configurar cómo cada columna obtiene su valor desde el record Solicitud
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id()));
        colSolicitante.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().solicitante()));
        colAsunto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().asunto()));
        colPrioridad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().prioridad().getEtiqueta()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado().getEtiqueta()));
        colAtendidaPor.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().atendidaPor() != null ? c.getValue().atendidaPor() : "-"
        ));

        // 3. Listener para autocompletar el formulario al hacer clic sobre una fila
        tblSolicitudes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                txtSolicitante.setText(seleccionada.solicitante());
                txtAsunto.setText(seleccionada.asunto());
                cmbPrioridad.setValue(seleccionada.prioridad());
            }
        });

        // 4. Mostrar datos iniciales en la tabla
        refrescarTabla();
    }

    /**
     * Método de inicialización por contrato con ContextoApp.
     */
    public void inicializar(ContextoApp contexto) {
        // Permite la inyección del contexto global de la aplicación
        refrescarTabla();
    }

    public void setServicio(ServicioSolicitudes servicio) {
        this.servicio = servicio;
        refrescarTabla();
    }

    public ServicioSolicitudes getServicio() {
        return this.servicio;
    }

    /**
     * Acción del botón Crear: valida los campos y registra una nueva solicitud.
     */
    @FXML
    private void onCrear() {
        try {
            String solicitante = txtSolicitante.getText();
            String asunto = txtAsunto.getText();
            Prioridad prioridad = cmbPrioridad.getValue();

            servicio.crear(solicitante, asunto, prioridad);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Solicitud registrada correctamente.");
            onLimpiar();
            refrescarTabla();
        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    /**
     * Acción del botón Actualizar: modifica la solicitud seleccionada si está PENDIENTE.
     */
    @FXML
    private void onActualizar() {
        Solicitud seleccionada = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Debe seleccionar una solicitud de la tabla para actualizar.");
            return;
        }

        try {
            String solicitante = txtSolicitante.getText();
            String asunto = txtAsunto.getText();
            Prioridad prioridad = cmbPrioridad.getValue();

            servicio.actualizar(seleccionada.id(), solicitante, asunto, prioridad);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Solicitud #" + seleccionada.id() + " actualizada con éxito.");
            refrescarTabla();
        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al actualizar: " + e.getMessage());
        }
    }

    /**
     * Acción del botón Eliminar: pide confirmación y elimina la solicitud seleccionada.
     */
    @FXML
    private void onEliminar() {
        Solicitud seleccionada = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Debe seleccionar una solicitud de la tabla para eliminar.");
            return;
        }

        boolean confirmado = mostrarConfirmacion(
                "Confirmar Eliminación",
                "¿Está seguro de que desea eliminar la solicitud #" + seleccionada.id() + " de '" + seleccionada.solicitante() + "'?"
        );

        if (!confirmado) {
            return;
        }

        try {
            servicio.eliminar(seleccionada.id());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Solicitud #" + seleccionada.id() + " eliminada.");
            onLimpiar();
            refrescarTabla();
        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Operación no permitida", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al eliminar: " + e.getMessage());
        }
    }

    /**
     * Acción del botón Limpiar: restablece los campos del formulario y desmarca la selección.
     */
    @FXML
    private void onLimpiar() {
        txtSolicitante.clear();
        txtAsunto.clear();
        cmbPrioridad.setValue(null);
        tblSolicitudes.getSelectionModel().clearSelection();
    }

    /**
     * Acción del botón Cargar Ejemplos: inserta 5 solicitudes ficticias para pruebas rápidas.
     */
    @FXML
    private void onCargarEjemplos() {
        try {
            int cantidad = servicio.cargarEjemplos(5);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Ejemplos agregados", "Se agregaron " + cantidad + " solicitudes de ejemplo.");
            refrescarTabla();
        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al cargar ejemplos: " + e.getMessage());
        }
    }

    /**
     * Relee los datos desde el servicio, actualiza la tabla conservando la selección y renueva el resumen.
     */
    public void refrescarTabla() {
        Solicitud seleccionada = tblSolicitudes.getSelectionModel().getSelectedItem();
        int idSeleccionado = seleccionada != null ? seleccionada.id() : -1;

        List<Solicitud> lista = servicio.listar();
        tblSolicitudes.getItems().setAll(lista);

        // Restaurar la selección por ID si aún se encuentra en la lista
        if (idSeleccionado != -1) {
            for (Solicitud s : lista) {
                if (s.id() == idSeleccionado) {
                    tblSolicitudes.getSelectionModel().select(s);
                    break;
                }
            }
        }

        // Actualizar el resumen en la barra inferior
        int pendientes = servicio.contar(EstadoSolicitud.PENDIENTE);
        int enProceso = servicio.contar(EstadoSolicitud.EN_PROCESO);
        int atendidas = servicio.contar(EstadoSolicitud.ATENDIDA);

        lblResumen.setText("Pendientes: " + pendientes + " · En proceso: " + enProceso + " · Atendidas: " + atendidas);
    }

    /**
     * Muestra una ventana de diálogo de alerta con un botón de Aceptar.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra una ventana de diálogo de confirmación con botones Aceptar y Cancelar.
     */
    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }
}
