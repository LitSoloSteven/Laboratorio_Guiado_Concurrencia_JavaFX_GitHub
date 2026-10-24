package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.ProcesoVentanilla;

public class DemoThreadRunnable {
    public static void main(String[] args) {
        ejecutarConRunDirecto();
        if (!Thread.currentThread().isInterrupted()) ejecutarConStart();
    }

    static void ejecutarConStart() { ProcesoVentanilla.ejecutarConStart(); }
    static void ejecutarConRunDirecto() { ProcesoVentanilla.ejecutarConRunDirecto(); }
}
