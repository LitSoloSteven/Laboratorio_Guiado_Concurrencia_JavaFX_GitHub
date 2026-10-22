package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo;

public class DemoEstadosHilo {

    public static void main(String[] args) {
        System.out.println("===== 1. Ciclo básico =====");
        observarCicloBasico();
        System.out.println("\n===== 2. TIMED_WAITING =====");
        observarTimedWaiting();
        System.out.println("\n===== 3. BLOCKED =====");
        observarBlocked();
        System.out.println("\n===== 4. WAITING =====");
        observarWaiting();
    }

    static void observarCicloBasico() {
        Thread hilo = new Thread(() -> dormir(400), "Hilo-Basico");

        System.out.println("PREDICCIÓN: antes de start() será NEW; durante el sleep, TIMED_WAITING; "
                + "tras join(), TERMINATED.");
        System.out.println("Antes de start():     " + hilo.getState());
        hilo.start();
        System.out.println("Justo tras start():   " + hilo.getState());
        dormir(150);
        System.out.println("Durante el sleep:     " + hilo.getState());
        esperar(hilo);
        System.out.println("Después del join():   " + hilo.getState());
    }

    static void observarTimedWaiting() {
        Thread hilo = new Thread(() -> dormir(1000), "Hilo-Dormilon");

        System.out.println("PREDICCIÓN: mientras dure el sleep(1000) todas las lecturas "
                + "serán TIMED_WAITING; al final, TERMINATED.");
        hilo.start();
        for (int i = 1; i <= 7; i++) {
            dormir(200);
            System.out.println("Lectura " + i + " (" + (i * 200) + " ms): " + hilo.getState());
        }
        esperar(hilo);
    }

    static void observarBlocked() {
        final Object candado = new Object();

        Thread dueno = new Thread(() -> {
            synchronized (candado) {
                dormir(1500);
            }
        }, "Hilo-Dueno");

        Thread bloqueado = new Thread(() -> {
            synchronized (candado) {
                System.out.println("  [Hilo-Bloqueado] por fin entró al synchronized");
            }
        }, "Hilo-Bloqueado");

        System.out.println("PREDICCIÓN: Hilo-Dueno estará TIMED_WAITING (durmiendo con el candado) "
                + "y Hilo-Bloqueado estará BLOCKED.");
        dueno.start();
        dormir(200);
        bloqueado.start();
        dormir(300);
        System.out.println("Hilo-Dueno:     " + dueno.getState());
        System.out.println("Hilo-Bloqueado: " + bloqueado.getState());
        esperar(dueno);
        esperar(bloqueado);
        System.out.println("Al final: " + dueno.getState() + " / " + bloqueado.getState());
    }

    static void observarWaiting() {
        final Object candado = new Object();
        final boolean[] listo = {false};

        Thread esperador = new Thread(() -> {
            synchronized (candado) {
                while (!listo[0]) {
                    try {
                        candado.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "Hilo-Esperador");

        System.out.println("PREDICCIÓN: Hilo-Esperador estará WAITING hasta que alguien haga notifyAll().");
        esperador.start();
        dormir(300);
        System.out.println("Hilo-Esperador con wait():  " + esperador.getState());
        synchronized (candado) {
            listo[0] = true;
            candado.notifyAll();
        }
        esperar(esperador);
        System.out.println("Hilo-Esperador al final:    " + esperador.getState());

        Thread largo = new Thread(() -> dormir(800), "Hilo-Largo");
        Thread quienEspera = new Thread(() -> esperar(largo), "Hilo-Join");
        largo.start();
        quienEspera.start();
        dormir(200);
        System.out.println("Hilo-Join con join():       " + quienEspera.getState());
        esperar(quienEspera);
        esperar(largo);
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void esperar(Thread hilo) {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
