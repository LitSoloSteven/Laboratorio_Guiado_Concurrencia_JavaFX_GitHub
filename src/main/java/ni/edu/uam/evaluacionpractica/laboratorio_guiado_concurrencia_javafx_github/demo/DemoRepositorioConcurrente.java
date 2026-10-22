package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.*;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudesMemoria;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class DemoRepositorioConcurrente {
    public static void main(String[] args) {
        try {
            ejecutar(args.length == 0 ? 1000 : Integer.parseInt(args[0]));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Demostración interrumpida.");
        }
    }

    static void ejecutar(int cantidadSolicitudes) throws InterruptedException {
        if (cantidadSolicitudes < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        var repositorio = new RepositorioSolicitudesMemoria();
        Prioridad[] prioridades = Prioridad.values();
        for (int i = 0; i < cantidadSolicitudes; i++) {
            repositorio.crear(Solicitud.nueva("Persona " + i, "Trámite " + i, prioridades[i % prioridades.length]));
        }
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        var contador = new ContadorCompartido();
        Runnable atender = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                String nombre = Thread.currentThread().getName();
                var siguiente = repositorio.tomarSiguientePendiente(nombre);
                if (siguiente.isEmpty()) return;
                int id = siguiente.orElseThrow().id();
                if (!ids.add(id)) throw new IllegalStateException("Atención duplicada: " + id);
                if (!repositorio.marcarAtendida(id, nombre)) throw new IllegalStateException("No se completó: " + id);
                contador.incrementar();
            }
        };
        var tareaA = new FutureTask<Void>(atender, null);
        var tareaB = new FutureTask<Void>(atender, null);
        Thread a = new Thread(tareaA, "Ventanilla-A");
        Thread b = new Thread(tareaB, "Ventanilla-B");
        a.start();
        b.start();
        try {
            a.join();
            b.join();
            tareaA.get();
            tareaB.get();
        } catch (InterruptedException e) {
            a.interrupt();
            b.interrupt();
            throw e;
        } catch (ExecutionException e) {
            throw new IllegalStateException("Falló una ventanilla.", e.getCause());
        }
        if (ids.size() != cantidadSolicitudes || contador.getTotal() != cantidadSolicitudes
                || repositorio.contarPorEstado(EstadoSolicitud.ATENDIDA) != cantidadSolicitudes
                || repositorio.contarPorEstado(EstadoSolicitud.EN_PROCESO) != 0) {
            throw new IllegalStateException("El total atendido no coincide con el total creado.");
        }
        System.out.printf("Solicitudes=%d, ids únicos=%d, atendidas=%d, duplicados=0%n",
                cantidadSolicitudes, ids.size(), contador.getTotal());
    }
}
