package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.MonitorEstadoHilo;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.ProcesoVentanilla;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util.RegistroEventos;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista.ActualizadorPeriodico;

public class PrincipalControlador {

    @FXML private Button btnIniciar;
    @FXML private Button btnDetener;
    @FXML private Label etiquetaEstado;
    @FXML private Label etiquetaContador;
    @FXML private TextArea areaRegistro;

    private ContadorCompartido contadorGlobal;
    private ProcesoVentanilla tareaVentanilla1;
    private ProcesoVentanilla tareaVentanilla2;
    private Thread hiloVentanilla1;
    private Thread hiloVentanilla2;

    private MonitorEstadoHilo monitor1;
    private MonitorEstadoHilo monitor2;
    private Thread hiloMonitor1;
    private Thread hiloMonitor2;

    private ActualizadorPeriodico actualizadorIU;

    @FXML
    public void iniciarProceso() {
        btnIniciar.setDisable(true);
        btnDetener.setDisable(false);
        etiquetaEstado.setText("Ejecutando");
        areaRegistro.clear();

        // 1. Inicializar recursos compartidos y tareas
        contadorGlobal = new ContadorCompartido();
        tareaVentanilla1 = new ProcesoVentanilla("Ventanilla-A", contadorGlobal);
        tareaVentanilla2 = new ProcesoVentanilla("Ventanilla-B", contadorGlobal);

        hiloVentanilla1 = new Thread(tareaVentanilla1, "Ventanilla-A");
        hiloVentanilla2 = new Thread(tareaVentanilla2, "Ventanilla-B");

        // 2. Inicializar monitores de estado (Estación 2)
        monitor1 = new MonitorEstadoHilo(hiloVentanilla1, mensaje -> RegistroEventos.registrar(areaRegistro, mensaje));
        monitor2 = new MonitorEstadoHilo(hiloVentanilla2, mensaje -> RegistroEventos.registrar(areaRegistro, mensaje));

        hiloMonitor1 = new Thread(monitor1, "Monitor-A");
        hiloMonitor2 = new Thread(monitor2, "Monitor-B");

        // 3. Inicializar y encender el actualizador periódico de interfaz (Estación 5)
        actualizadorIU = new ActualizadorPeriodico(etiquetaContador, contadorGlobal);
        actualizadorIU.start();

        // 4. Iniciar todos los hilos
        RegistroEventos.registrar(areaRegistro, "Sistema iniciado.");
        hiloMonitor1.start();
        hiloMonitor2.start();
        hiloVentanilla1.start();
        hiloVentanilla2.start();
    }

    @FXML
    public void detenerProceso() {
        btnDetener.setDisable(true);
        etiquetaEstado.setText("Deteniendo...");
        RegistroEventos.registrar(areaRegistro, "Solicitando detención cooperativa de ventanillas...");

        // 1. Aplicar bandera volatile
        tareaVentanilla1.detener();
        tareaVentanilla2.detener();
        monitor1.detener();
        monitor2.detener();

        // 2. Aplicar interrupción de Sleep (Estación 4)
        hiloVentanilla1.interrupt();
        hiloVentanilla2.interrupt();
        hiloMonitor1.interrupt();
        hiloMonitor2.interrupt();

        // 3. Detener animador visual y reactivar inicio
        if (actualizadorIU != null) {
            actualizadorIU.stop();
            // Actualización final forzada para asegurar el valor exacto tras detener
            etiquetaContador.setText(String.valueOf(contadorGlobal.getTotal()));
        }

        etiquetaEstado.setText("Detenido");
        btnIniciar.setDisable(false);
    }
}