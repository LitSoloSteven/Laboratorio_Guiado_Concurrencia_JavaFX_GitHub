package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import java.util.Objects;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Contador;

public class TareaIncremento implements Runnable {

    private final Contador contador;
    private final int repeticiones;

    public TareaIncremento(Contador contador, int repeticiones) {
        this.contador = Objects.requireNonNull(contador, "contador no puede ser null");
        if (repeticiones < 0) {
            throw new IllegalArgumentException("repeticiones no puede ser negativo: " + repeticiones);
        }
        this.repeticiones = repeticiones;
    }

    @Override
    public void run() {
        for (int i = 0; i < repeticiones; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            contador.incrementar();
        }
    }
}
