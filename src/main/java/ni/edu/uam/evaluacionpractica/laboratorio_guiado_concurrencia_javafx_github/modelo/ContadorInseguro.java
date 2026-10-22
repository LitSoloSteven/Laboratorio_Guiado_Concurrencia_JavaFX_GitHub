package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

public class ContadorInseguro implements Contador {

    private int total = 0;

    @Override
    public void incrementar() {
        total++;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public void reiniciar() {
        total = 0;
    }
}
