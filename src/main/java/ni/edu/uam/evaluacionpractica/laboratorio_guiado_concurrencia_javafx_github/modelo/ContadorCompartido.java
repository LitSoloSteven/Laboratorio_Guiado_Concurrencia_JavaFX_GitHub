package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

public class ContadorCompartido implements Contador {

    private int total = 0;

    @Override
    public synchronized void incrementar() {
        total++;
    }

    @Override
    public synchronized int getTotal() {
        return total;
    }

    @Override
    public synchronized void reiniciar() {
        total = 0;
    }
}
