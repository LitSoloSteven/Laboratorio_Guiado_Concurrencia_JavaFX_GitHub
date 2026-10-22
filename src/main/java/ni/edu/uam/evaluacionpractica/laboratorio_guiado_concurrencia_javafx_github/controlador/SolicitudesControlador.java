package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista.Alertas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app.ContextoApp;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ReglaNegocioException;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ServicioSolicitudes;

import java.util.List;

public class SolicitudesControlador {

    @FXML private TextField txtSolicitante;
    @FXML private TextField txtAsunto;
    @FXML private ComboBox<Prioridad> cmbPrioridad;

    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCargarEjemplos;

    @FXML private TableView<Solicitud> tblSolicitudes;
    @FXML private TableColumn<Solicitud, Number> colId;
    @FXML private TableColumn<Solicitud, String> colSolicitante;
    @FXML private TableColumn<Solicitud, String> colAsunto;
    @FXML private TableColumn<Solicitud, String> colPrioridad;
    @FXML private TableColumn<Solicitud, String> colEstado;
    @FXML private TableColumn<Solicitud, String> colAtendidaPor;

    @FXML private Label lblResumen;

    private ServicioSolicitudes servicio;
    private boolean refrescando;

    @FXML
    public void initialize() {

        cmbPrioridad.getItems().setAll(Prioridad.values());
        tblSolicitudes.setPlaceholder(new Label("No hay solicitudes registradas."));

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id()));
        colSolicitante.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().solicitante()));
        colAsunto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().asunto()));
        colPrioridad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().prioridad().getEtiqueta()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado().getEtiqueta()));
        colAtendidaPor.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().atendidaPor() != null ? c.getValue().atendidaPor() : "-"
        ));

        tblSolicitudes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null && !refrescando) {
                txtSolicitante.setText(seleccionada.solicitante());
                txtAsunto.setText(seleccionada.asunto());
                cmbPrioridad.setValue(seleccionada.prioridad());
            }
        });

        refrescarTabla();
    }

    public void inicializar(ContextoApp contexto) {
        servicio = java.util.Objects.requireNonNull(contexto.servicio());
        contexto.actualizador().agregarTarea(this::refrescarTabla);

        refrescarTabla();
    }

    public void setServicio(ServicioSolicitudes servicio) {
        this.servicio = servicio;
        refrescarTabla();
    }

    public ServicioSolicitudes getServicio() {
        return this.servicio;
    }

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

    @FXML
    private void onLimpiar() {
        txtSolicitante.clear();
        txtAsunto.clear();
        cmbPrioridad.setValue(null);
        tblSolicitudes.getSelectionModel().clearSelection();
    }

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

    public void refrescarTabla() {
        if (servicio == null) return;
        Solicitud seleccionada = tblSolicitudes.getSelectionModel().getSelectedItem();
        int idSeleccionado = seleccionada != null ? seleccionada.id() : -1;

        List<Solicitud> lista = servicio.listar();
        refrescando = true;
        try {
            tblSolicitudes.getItems().setAll(lista);

            if (idSeleccionado != -1) {
                for (Solicitud s : lista) {
                    if (s.id() == idSeleccionado) {
                        tblSolicitudes.getSelectionModel().select(s);
                        break;
                    }
                }
            }
        } finally {
            refrescando = false;
        }

        int pendientes = servicio.contar(EstadoSolicitud.PENDIENTE);
        int enProceso = servicio.contar(EstadoSolicitud.EN_PROCESO);
        int atendidas = servicio.contar(EstadoSolicitud.ATENDIDA);

        lblResumen.setText("Pendientes: " + pendientes + " · En proceso: " + enProceso + " · Atendidas: " + atendidas);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        switch (tipo) {
            case INFORMATION -> Alertas.informacion(titulo, mensaje);
            case WARNING -> Alertas.advertencia(titulo, mensaje);
            default -> Alertas.error(titulo, mensaje);
        }
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        return Alertas.confirmar(titulo, mensaje);
    }
}
