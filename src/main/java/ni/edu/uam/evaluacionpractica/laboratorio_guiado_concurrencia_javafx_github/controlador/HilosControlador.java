package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.controlador;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app.ContextoApp;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSesion;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.GestorVentanillas;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.MonitorEstadoHilo;

public class HilosControlador {

    private static final long INTERVALO_MONITOR_MS = 50;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @FXML private Label lblHiloA;
    @FXML private Label lblHiloB;
    @FXML private TextArea areaHistorialEstados;

    private final Queue<String> historialPendiente = new ConcurrentLinkedQueue<>();
    private final Map<String, Thread.State> ultimoEstado = new ConcurrentHashMap<>();

    private final List<MonitorEstadoHilo> monitores = new ArrayList<>();
    private final List<Thread> hilosMonitor = new ArrayList<>();

    public void inicializar(ContextoApp contexto) {
        areaHistorialEstados.setEditable(false);
        GestorVentanillas gestor = contexto.gestor();

        gestor.agregarOyenteEstado(estado -> {
            if (estado == EstadoSesion.EJECUTANDO) {
                iniciarMonitores(gestor);
            }
        });

        contexto.actualizador().agregarTarea(this::refrescarVista);
    }

    private synchronized void iniciarMonitores(GestorVentanillas gestor) {
        detenerMonitores();
        ultimoEstado.clear();
        historialPendiente.add("---- Nueva sesión ----");

        crearMonitor(gestor.getHiloA());
        crearMonitor(gestor.getHiloB());
    }

    private void crearMonitor(Thread objetivo) {
        if (objetivo == null) {
            return;
        }
        MonitorEstadoHilo monitor = new MonitorEstadoHilo(objetivo, INTERVALO_MONITOR_MS,
                (nombre, estado) -> {

                    ultimoEstado.put(nombre, estado);
                    historialPendiente.add(LocalTime.now().format(HORA) + "  "
                            + nombre + " -> " + estado);
                });
        Thread hilo = new Thread(monitor, "Monitor-" + objetivo.getName());
        hilo.setDaemon(true);
        monitores.add(monitor);
        hilosMonitor.add(hilo);
        hilo.start();
    }

    public void refrescarVista() {
        StringBuilder nuevo = new StringBuilder();
        String linea;
        while ((linea = historialPendiente.poll()) != null) {
            nuevo.append(linea).append('\n');
        }
        if (nuevo.length() > 0) {
            areaHistorialEstados.appendText(nuevo.toString());
        }
        lblHiloA.setText(textoEstado(GestorVentanillas.NOMBRE_VENTANILLA_A));
        lblHiloB.setText(textoEstado(GestorVentanillas.NOMBRE_VENTANILLA_B));
    }

    private String textoEstado(String nombreHilo) {
        Thread.State estado = ultimoEstado.get(nombreHilo);
        return estado == null ? "Sin iniciar" : estado.name();
    }

    public synchronized void detenerMonitores() {
        for (MonitorEstadoHilo monitor : monitores) {
            monitor.detener();
        }
        for (Thread hilo : hilosMonitor) {
            hilo.interrupt();
        }
        monitores.clear();
        hilosMonitor.clear();
    }
}
