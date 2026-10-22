package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

public class ContadorCompartido {
    private int total = 0;

    public synchronized void incrementar() {
        total++;
    }

    public synchronized int getTotal() {
        return total;
    }
}