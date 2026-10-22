package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Contador;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudes;
import java.util.Objects;
import java.util.function.Consumer;

public class Ventanilla implements Runnable {
    public static final long TIEMPO_PROCESO_MS = 250;
    private final String nombre;
    private final RepositorioSolicitudes repositorio;
    private final Contador contador;
    private final Consumer<String> registro;
    private volatile boolean activo = true;
    private volatile int atendidas;

    public Ventanilla(String nombre, RepositorioSolicitudes repositorio, Contador contador, Consumer<String> registro) {
        this.nombre = Objects.requireNonNull(nombre);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.contador = Objects.requireNonNull(contador);
        this.registro = Objects.requireNonNull(registro);
    }

    @Override public void run() {
        while (activo && !Thread.currentThread().isInterrupted()) {
            var siguiente = repositorio.tomarSiguientePendiente(nombre);
            if (siguiente.isEmpty()) return;
            int id = siguiente.orElseThrow().id();
            boolean completada = false;
            try {
                if (!activo || Thread.currentThread().isInterrupted()) return;
                Thread.sleep(TIEMPO_PROCESO_MS);
                if (!activo || Thread.currentThread().isInterrupted()) return;
                if (repositorio.marcarAtendida(id, nombre)) {
                    completada = true;
                    atendidas++;
                    contador.incrementar();
                    registro.accept(nombre + " atendió la solicitud " + id);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                if (!completada) repositorio.devolverAPendiente(id);
            }
        }
    }

    public void detener() { activo = false; }
    public String getNombre() { return nombre; }
    public int getAtendidas() { return atendidas; }
}
