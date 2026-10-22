package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.*;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.TareaIncremento;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public class DemoCondicionCarrera {
    public static void main(String[] args) {
        try {
            repetirPrueba("Sin sincronizar", ContadorInseguro::new, 10);
            repetirPrueba("Con synchronized", ContadorCompartido::new, 10);
            repetirPrueba("Con AtomicInteger", ContadorAtomico::new, 10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Demostración interrumpida.");
        }
    }

    static int ejecutarPrueba(Contador contador, int repeticionesPorHilo) throws InterruptedException {
        Objects.requireNonNull(contador);
        TareaIncremento tarea = new TareaIncremento(contador, repeticionesPorHilo);
        contador.reiniciar();
        CountDownLatch inicio = new CountDownLatch(1);
        Runnable ejecutar = () -> {
            try {
                inicio.await();
                tarea.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        Thread a = new Thread(ejecutar, "Ventanilla-A");
        Thread b = new Thread(ejecutar, "Ventanilla-B");
        a.start();
        b.start();
        inicio.countDown();
        try {
            a.join();
            b.join();
        } catch (InterruptedException e) {
            a.interrupt();
            b.interrupt();
            throw e;
        }
        return contador.getTotal();
    }

    static void repetirPrueba(String titulo, Supplier<Contador> fabrica, int veces) throws InterruptedException {
        if (veces < 0) throw new IllegalArgumentException("veces no puede ser negativo");
        System.out.println(titulo);
        for (int i = 1; i <= veces; i++) {
            Contador contador = fabrica.get();
            int total = ejecutarPrueba(contador, 1000);
            System.out.printf("Prueba %02d: esperado=2000, obtenido=%d, perdidos=%d%n", i, total, 2000 - total);
            if (!(contador instanceof ContadorInseguro) && total != 2000) {
                throw new IllegalStateException("El contador seguro perdió incrementos.");
            }
        }
    }
}
