package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSesion;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudesMemoria;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.ResultadoOperacion;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.GestorVentanillas;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.TareaIncremento;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util.RegistroEventos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(15)
class ConcurrenciaIntegrante3Test {
    private Solicitud crear(RepositorioSolicitudesMemoria repo, Prioridad prioridad) {
        return repo.crear(Solicitud.nueva("Persona", "Trámite", prioridad));
    }

    @Test void prioridadIdYReindexacionAlEditar() {
        var repo = new RepositorioSolicitudesMemoria();
        var baja = crear(repo, Prioridad.BAJA);
        var alta1 = crear(repo, Prioridad.ALTA);
        var alta2 = crear(repo, Prioridad.ALTA);
        var media = crear(repo, Prioridad.MEDIA);
        assertEquals(ResultadoOperacion.OK, repo.actualizar(baja.conDatos("Editada", "Trámite", Prioridad.ALTA)));
        assertEquals(baja.id(), repo.tomarSiguientePendiente("A").orElseThrow().id());
        assertEquals(alta1.id(), repo.tomarSiguientePendiente("B").orElseThrow().id());
        assertEquals(alta2.id(), repo.tomarSiguientePendiente("A").orElseThrow().id());
        assertEquals(media.id(), repo.tomarSiguientePendiente("B").orElseThrow().id());
        assertTrue(repo.tomarSiguientePendiente("A").isEmpty());
    }

    @Test void snapshotYEstadosDelRepositorio() {
        var repo = new RepositorioSolicitudesMemoria();
        var original = crear(repo, Prioridad.ALTA);
        var snapshot = repo.listar();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(ResultadoOperacion.NO_ENCONTRADA, repo.actualizar(original.conId(999)));
        assertEquals(ResultadoOperacion.NO_ENCONTRADA, repo.eliminar(999));
        assertFalse(repo.marcarAtendida(original.id(), "A"));
        assertFalse(repo.devolverAPendiente(original.id()));
        repo.tomarSiguientePendiente("A");
        assertEquals(ResultadoOperacion.BLOQUEADA, repo.eliminar(original.id()));
        assertEquals(ResultadoOperacion.BLOQUEADA, repo.actualizar(original));
        assertTrue(repo.devolverAPendiente(original.id()));
        assertNull(repo.buscarPorId(original.id()).orElseThrow().atendidaPor());
        repo.tomarSiguientePendiente("B");
        assertTrue(repo.marcarAtendida(original.id(), "B"));
        assertFalse(repo.marcarAtendida(original.id(), "B"));
        assertFalse(repo.devolverAPendiente(original.id()));
        assertEquals(EstadoSolicitud.PENDIENTE, snapshot.getFirst().estado());
        assertEquals(ResultadoOperacion.BLOQUEADA, repo.actualizar(original));
        assertEquals(ResultadoOperacion.OK, repo.eliminar(original.id()));
        assertTrue(repo.listar().isEmpty());
    }

    @Test void eliminarPendienteNoDejaIdsEnIndice() {
        var repo = new RepositorioSolicitudesMemoria();
        var solicitud = crear(repo, Prioridad.ALTA);
        assertEquals(ResultadoOperacion.OK, repo.eliminar(solicitud.id()));
        assertTrue(repo.tomarSiguientePendiente("A").isEmpty());
        assertTrue(crear(repo, Prioridad.MEDIA).id() > solicitud.id());
    }

    @Test void creacionSimultaneaConIdsUnicos() throws Exception {
        var repo = new RepositorioSolicitudesMemoria();
        try (var executor = Executors.newFixedThreadPool(4)) {
            var resultados = new ArrayList<Future<?>>();
            for (int hilo = 0; hilo < 4; hilo++) resultados.add(executor.submit(() -> {
                for (int i = 0; i < 1000; i++) crear(repo, Prioridad.MEDIA);
            }));
            for (var resultado : resultados) resultado.get(10, TimeUnit.SECONDS);
        }
        var lista = repo.listar();
        assertEquals(4000, lista.size());
        for (int i = 0; i < lista.size(); i++) assertEquals(i + 1, lista.get(i).id());
    }

    @Test void registroConcurrenteSinMensajesPerdidosODuplicados() throws Exception {
        var registro = new RegistroEventos();
        Set<String> recibidos = new HashSet<>();
        try (var executor = Executors.newFixedThreadPool(4)) {
            var resultados = new ArrayList<Future<?>>();
            for (int hilo = 0; hilo < 4; hilo++) {
                final int id = hilo;
                resultados.add(executor.submit(() -> {
                    for (int i = 0; i < 1000; i++) registro.publicar(id + ":" + i);
                }));
            }
            while (resultados.stream().anyMatch(f -> !f.isDone())) {
                for (String mensaje : registro.vaciar()) assertTrue(recibidos.add(mensaje));
                Thread.yield();
            }
            for (var resultado : resultados) resultado.get(10, TimeUnit.SECONDS);
            for (String mensaje : registro.vaciar()) assertTrue(recibidos.add(mensaje));
        }
        assertEquals(4000, recibidos.size());
        assertTrue(registro.vaciar().isEmpty());
        assertThrows(NullPointerException.class, () -> registro.publicar(null));
        registro.publicar("primero"); registro.publicar("segundo");
        assertEquals(List.of("primero", "segundo"), registro.vaciar());
    }

    @Test void tareaValidaParametrosYRespetaInterrupcion() throws Exception {
        var contador = new ContadorCompartido();
        assertThrows(NullPointerException.class, () -> new TareaIncremento(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new TareaIncremento(contador, -1));
        new TareaIncremento(contador, 0).run();
        assertEquals(0, contador.getTotal());
        var hilo = new Thread(() -> {
            Thread.currentThread().interrupt();
            new TareaIncremento(contador, 1000).run();
        });
        hilo.start(); hilo.join(2000);
        assertFalse(hilo.isAlive());
        assertEquals(0, contador.getTotal());
    }

    @Test void cancelarYReiniciarSesionNoDejaSolicitudesEnProceso() throws Exception {
        var repo = new RepositorioSolicitudesMemoria();
        for (int i = 0; i < 4; i++) crear(repo, Prioridad.MEDIA);
        var contador = new ContadorCompartido();
        var gestor = new GestorVentanillas(repo, contador, new RegistroEventos()::publicar);
        var cambios = new CopyOnWriteArrayList<EstadoSesion>();
        gestor.agregarOyenteEstado(cambios::add);
        gestor.iniciar();
        assertThrows(IllegalStateException.class, gestor::iniciar);
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (repo.contarPorEstado(EstadoSolicitud.EN_PROCESO) == 0 && System.nanoTime() < limite) Thread.sleep(1);
        assertTrue(repo.contarPorEstado(EstadoSolicitud.EN_PROCESO) > 0);
        gestor.detener();
        esperarFin(gestor);
        assertEquals(EstadoSesion.DETENIDO, gestor.getEstado());
        assertEquals(0, repo.contarPorEstado(EstadoSolicitud.EN_PROCESO));
        assertTrue(gestor.puedeIniciar());
        assertTrue(cambios.containsAll(List.of(EstadoSesion.EJECUTANDO, EstadoSesion.DETENIENDO, EstadoSesion.DETENIDO)));
        int anteriores = repo.contarPorEstado(EstadoSolicitud.ATENDIDA);
        gestor.iniciar();
        esperarFin(gestor);
        assertEquals(EstadoSesion.FINALIZADO, gestor.getEstado());
        assertEquals(4, repo.contarPorEstado(EstadoSolicitud.ATENDIDA));
        assertEquals(4 - anteriores, contador.getTotal());
        assertEquals(contador.getTotal(), gestor.getAtendidasA() + gestor.getAtendidasB());
    }

    private void esperarFin(GestorVentanillas gestor) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (gestor.hayHilosVivos() && System.nanoTime() < limite) Thread.sleep(1);
        if (gestor.hayHilosVivos()) gestor.detener();
        assertFalse(gestor.hayHilosVivos());
    }
}
