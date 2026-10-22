package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas;

public class ProcesoVentanilla implements Runnable {

    private final String nombre;

    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public void run() {
        String hiloActual = Thread.currentThread().getName();
        System.out.println("[" + hiloActual + "] Iniciando tarea: " + nombre);

        for (int i = 1; i <= 5; i++) {

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("[" + hiloActual + "] " + nombre + " detenido por interrupción.");
                return;
            }

            System.out.println("[" + hiloActual + "] " + nombre + " procesando paso " + i + "/5");

            try {

                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("[" + hiloActual + "] " + nombre + " fue interrumpido durante sleep.");

                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("[" + hiloActual + "] " + nombre + " finalizó correctamente.");
    }

    public static void ejecutarConStart() {
        System.out.println("=== DEMOSTRACIÓN CON START() (CONCURRENTE) ===");
        Thread hilo1 = new Thread(new ProcesoVentanilla("Ventanilla-A"), "Ventanilla-A");
        Thread hilo2 = new Thread(new ProcesoVentanilla("Ventanilla-B"), "Ventanilla-B");

        hilo1.start();
        hilo2.start();

        try {

            hilo1.join();
            hilo2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void ejecutarConRunDirecto() {
        System.out.println("=== DEMOSTRACIÓN CON RUN() DIRECTO (SECUENCIAL) ===");
        ProcesoVentanilla v1 = new ProcesoVentanilla("Ventanilla-A");
        ProcesoVentanilla v2 = new ProcesoVentanilla("Ventanilla-B");

        v1.run();
        v2.run();
    }

    public static void main(String[] args) {

        ejecutarConRunDirecto();

        System.out.println();

        ejecutarConStart();
    }
}
