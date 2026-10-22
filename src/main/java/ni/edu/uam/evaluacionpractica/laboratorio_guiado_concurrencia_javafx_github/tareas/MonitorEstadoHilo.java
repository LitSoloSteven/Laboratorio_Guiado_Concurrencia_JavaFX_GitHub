package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import java.util.Objects;
import java.util.function.BiConsumer;

/** Estación 2. Consulta getState() cada 'intervaloMs' sin bloquear el hilo gráfico. */
public class MonitorEstadoHilo implements Runnable {

    private final Thread objetivo;
    private final long intervaloMs;
    private final BiConsumer<String, Thread.State> receptor;
    private volatile boolean activo = true;

    public MonitorEstadoHilo(Thread objetivo, long intervaloMs,
                             BiConsumer<String, Thread.State> receptor) {
        this.objetivo = Objects.requireNonNull(objetivo, "objetivo");
        this.receptor = Objects.requireNonNull(receptor, "receptor");
        if (intervaloMs <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que 0");
        }
        this.intervaloMs = intervaloMs;
    }

    @Override
    public void run() {
        Thread.State anterior = null;
        while (activo) {
            Thread.State actual = objetivo.getState();
            if (anterior == null || actual != anterior) {
                receptor.accept(objetivo.getName(), actual);
                anterior = actual;
            }
            if (actual == Thread.State.TERMINATED) {
                return;
            }
            try {
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();   // nunca se ignora
                return;
            }
        }
    }

    public void detener() {
        activo = false;
    }
}