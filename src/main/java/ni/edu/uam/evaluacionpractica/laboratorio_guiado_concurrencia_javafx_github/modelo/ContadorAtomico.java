package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorAtomico implements Contador {

    private final AtomicInteger total = new AtomicInteger(0);

    @Override
    public void incrementar() {
        total.incrementAndGet();
    }

    @Override
    public int getTotal() {
        return total.get();
    }

    @Override
    public void reiniciar() {
        total.set(0);
    }
}
