package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

/**
 * Tarea finita que representa el trabajo de una ventanilla.
 * Implementa Runnable para ser ejecutada concurrentemente mediante hilos (Thread).
 * Cumple con el objetivo de la Estación 1: comprensión de Thread y Runnable.
 */
public class ProcesoVentanilla implements Runnable {

    // Nombre identificador de la ventanilla o tarea
    private final String nombre;

    /**
     * Constructor que asigna el nombre a la tarea de ventanilla.
     * @param nombre Identificador textual del proceso.
     */
    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Lógica de ejecución del hilo: realiza 5 iteraciones con pausa de 500 ms.
     * Revisa en cada ciclo si el hilo fue interrumpido.
     */
    @Override
    public void run() {
        String hiloActual = Thread.currentThread().getName();
        System.out.println("[" + hiloActual + "] Iniciando tarea: " + nombre);

        for (int i = 1; i <= 5; i++) {
            // Verificar si se solicitó detener el hilo
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[" + hiloActual + "] " + nombre + " detenido por interrupción.");
                return;
            }

            System.out.println("[" + hiloActual + "] " + nombre + " procesando paso " + i + "/5");

            try {
                // Simulación de trabajo con pausa de 500 milisegundos
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("[" + hiloActual + "] " + nombre + " fue interrumpido durante sleep.");
                // Buena práctica: restaurar la bandera de interrupción antes de terminar
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("[" + hiloActual + "] " + nombre + " finalizó correctamente.");
    }

    /**
     * Demostración con start(): crea hilos independientes y se ejecutan en paralelo.
     */
    public static void ejecutarConStart() {
        System.out.println("=== DEMOSTRACIÓN CON START() (CONCURRENTE) ===");
        Thread hilo1 = new Thread(new ProcesoVentanilla("Ventanilla-A"), "Hilo-Ventanilla-A");
        Thread hilo2 = new Thread(new ProcesoVentanilla("Ventanilla-B"), "Hilo-Ventanilla-B");

        hilo1.start();
        hilo2.start();

        try {
            // Esperamos a que ambos hilos terminen para la demostración
            hilo1.join();
            hilo2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demostración con run() directo: NO crea hilos nuevos, se ejecuta secuencialmente en el hilo actual.
     */
    public static void ejecutarConRunDirecto() {
        System.out.println("=== DEMOSTRACIÓN CON RUN() DIRECTO (SECUENCIAL) ===");
        ProcesoVentanilla v1 = new ProcesoVentanilla("Ventanilla-A");
        ProcesoVentanilla v2 = new ProcesoVentanilla("Ventanilla-B");

        // Al llamar a run() directamente, se ejecuta en el hilo 'main'
        v1.run();
        v2.run();
    }

    /**
     * Punto de entrada de prueba para ejecutar y comprobar el comportamiento de la Estación 1.
     */
    public static void main(String[] args) {
        // Ejecución secuencial sin hilos adicionales
        ejecutarConRunDirecto();

        System.out.println();

        // Ejecución concurrente usando hilos Thread
        ejecutarConStart();
    }
}
