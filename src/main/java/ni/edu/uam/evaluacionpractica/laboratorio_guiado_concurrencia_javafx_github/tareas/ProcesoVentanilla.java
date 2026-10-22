package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;

public class ProcesoVentanilla implements Runnable {
    private final String nombre;
    private final ContadorCompartido contador;
    private volatile boolean activo = true;

    public ProcesoVentanilla(String nombre, ContadorCompartido contador) {
        this.nombre = nombre;
        this.contador = contador;
    }

    public void detener() {
        activo = false;
    }

    @Override
    public void run() {
        while (activo && !Thread.currentThread().isInterrupted()) {
            contador.incrementar();

            try {
                // Simula el tiempo de procesamiento de una solicitud
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // Restablecer la interrupción y salir del ciclo
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}