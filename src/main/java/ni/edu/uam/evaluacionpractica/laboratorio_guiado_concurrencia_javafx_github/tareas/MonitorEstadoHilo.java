package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import javafx.application.Platform;
import java.util.function.Consumer;

public class MonitorEstadoHilo implements Runnable {
    private final Thread hiloObservado;
    private volatile boolean activo = true;
    private final Consumer<String> notificadorUI;

    public MonitorEstadoHilo(Thread hiloObservado, Consumer<String> notificadorUI) {
        this.hiloObservado = hiloObservado;
        this.notificadorUI = notificadorUI;
    }

    public void detener() {
        activo = false;
    }

    @Override
    public void run() {
        Thread.State estadoAnterior = null;

        while (activo && !Thread.currentThread().isInterrupted()) {
            Thread.State estadoActual = hiloObservado.getState();

            if (estadoActual != estadoAnterior) {
                String mensaje = "Estado de " + hiloObservado.getName() + " cambió a: " + estadoActual;
                Platform.runLater(() -> notificadorUI.accept(mensaje));
                estadoAnterior = estadoActual;
            }

            if (estadoActual == Thread.State.TERMINATED) {
                break;
            }

            try {
                Thread.sleep(50); // Consulta frecuente para capturar transiciones rápidas
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}