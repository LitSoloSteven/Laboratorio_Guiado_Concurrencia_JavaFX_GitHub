package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Contador;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSesion;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudes;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GestorVentanillas {
    public static final String NOMBRE_VENTANILLA_A = "Ventanilla-A";
    public static final String NOMBRE_VENTANILLA_B = "Ventanilla-B";
    public static final String NOMBRE_SUPERVISOR = "Supervisor-Sesion";
    private final RepositorioSolicitudes repositorio;
    private final Contador contador;
    private final Consumer<String> registro;
    private final CopyOnWriteArrayList<Consumer<EstadoSesion>> oyentes = new CopyOnWriteArrayList<>();
    private EstadoSesion estado = EstadoSesion.EN_ESPERA;
    private Ventanilla ventanillaA;
    private Ventanilla ventanillaB;
    private Thread hiloA;
    private Thread hiloB;
    private Thread supervisor;

    public GestorVentanillas(RepositorioSolicitudes repositorio, Contador contador, Consumer<String> registro) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.contador = Objects.requireNonNull(contador);
        this.registro = Objects.requireNonNull(registro);
    }

    public void agregarOyenteEstado(Consumer<EstadoSesion> oyente) { oyentes.add(Objects.requireNonNull(oyente)); }

    public synchronized void iniciar() {
        if (!puedeIniciar()) throw new IllegalStateException("Hay una sesión activa o hilos por terminar.");
        contador.reiniciar();
        ventanillaA = new Ventanilla(NOMBRE_VENTANILLA_A, repositorio, contador, registro);
        ventanillaB = new Ventanilla(NOMBRE_VENTANILLA_B, repositorio, contador, registro);
        hiloA = new Thread(ventanillaA, NOMBRE_VENTANILLA_A);
        hiloB = new Thread(ventanillaB, NOMBRE_VENTANILLA_B);
        supervisor = new Thread(this::supervisar, NOMBRE_SUPERVISOR);
        estado = EstadoSesion.EJECUTANDO;
        hiloA.start();
        hiloB.start();
        notificar(estado);
        supervisor.start();
    }

    public synchronized void detener() {
        if (estado != EstadoSesion.EJECUTANDO) return;
        estado = EstadoSesion.DETENIENDO;
        ventanillaA.detener();
        ventanillaB.detener();
        hiloA.interrupt();
        hiloB.interrupt();
        notificar(estado);
    }

    private void supervisar() {
        boolean interrumpido = false;
        for (Thread hilo : new Thread[] { hiloA, hiloB }) {
            while (hilo.isAlive()) {
                try { hilo.join(); } catch (InterruptedException e) {
                    interrumpido = true;
                    detener();
                }
            }
        }
        synchronized (this) {
            estado = estado == EstadoSesion.DETENIENDO ? EstadoSesion.DETENIDO : EstadoSesion.FINALIZADO;
            notificar(estado);
        }
        if (interrumpido) Thread.currentThread().interrupt();
    }

    private void notificar(EstadoSesion nuevoEstado) {
        for (Consumer<EstadoSesion> oyente : oyentes) {
            try { oyente.accept(nuevoEstado); } catch (RuntimeException e) {
                registro.accept("Falló un oyente de estado: " + e.getMessage());
            }
        }
    }

    public synchronized boolean puedeIniciar() {
        return estado != EstadoSesion.EJECUTANDO && estado != EstadoSesion.DETENIENDO && !hayHilosVivos();
    }
    public synchronized boolean hayHilosVivos() {
        return hiloA != null && hiloA.isAlive() || hiloB != null && hiloB.isAlive()
                || supervisor != null && supervisor.isAlive();
    }
    public synchronized EstadoSesion getEstado() { return estado; }
    public synchronized Thread getHiloA() { return hiloA; }
    public synchronized Thread getHiloB() { return hiloB; }
    public synchronized int getAtendidasA() { return ventanillaA == null ? 0 : ventanillaA.getAtendidas(); }
    public synchronized int getAtendidasB() { return ventanillaB == null ? 0 : ventanillaB.getAtendidas(); }
}
