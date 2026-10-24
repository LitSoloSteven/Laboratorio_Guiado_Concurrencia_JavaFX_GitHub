package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Contador;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorAtomico;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudesMemoria;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ReglaNegocioException;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ServicioSolicitudes;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.ProcesoVentanilla;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.TareaIncremento;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.Ventanilla;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegracionSolicitudesTest {
    @Test void crudYDatosInmutables() {
        var repo = new RepositorioSolicitudesMemoria();
        var servicio = new ServicioSolicitudes(repo);
        var creada = servicio.crear(" Ana ", " Consulta ", Prioridad.MEDIA);
        assertEquals("Ana", creada.solicitante());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.estado());
        var snapshot = servicio.listar();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        var editada = servicio.actualizar(creada.id(), "Luis", "Cambio", Prioridad.ALTA);
        assertEquals(creada.creadaEn(), editada.creadaEn());
        assertEquals("Ana", snapshot.getFirst().solicitante());
        assertEquals("Luis", repo.buscarPorId(creada.id()).orElseThrow().solicitante());
        servicio.eliminar(creada.id());
        assertTrue(servicio.buscar(creada.id()).isEmpty());
    }

    @Test void validacionesYLimites() {
        var servicio = new ServicioSolicitudes(new RepositorioSolicitudesMemoria());
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(" ", "Asunto", Prioridad.BAJA));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(null, "Asunto", Prioridad.BAJA));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("Ana", " ", Prioridad.BAJA));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("Ana", null, Prioridad.BAJA));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("Ana", "Asunto", null));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("a".repeat(41), "Asunto", Prioridad.BAJA));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("Ana", "a".repeat(81), Prioridad.BAJA));
        servicio.crear("a".repeat(40), "a".repeat(80), Prioridad.BAJA);
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(999, "Ana", "Asunto", Prioridad.MEDIA));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(999));
    }

    @Test void bloqueoYCancelacion() {
        var repo = new RepositorioSolicitudesMemoria();
        var servicio = new ServicioSolicitudes(repo);
        var creada = servicio.crear("Ana", "Asunto", Prioridad.MEDIA);
        repo.tomarSiguientePendiente("Ventanilla-A");
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(creada.id(), "Ana", "Otro", Prioridad.ALTA));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(creada.id()));
        assertTrue(repo.devolverAPendiente(creada.id()));
        assertNull(servicio.buscar(creada.id()).orElseThrow().atendidaPor());
        servicio.actualizar(creada.id(), "Ana", "Otro", Prioridad.ALTA);
        repo.tomarSiguientePendiente("Ventanilla-B");
        assertTrue(repo.marcarAtendida(creada.id(), "Ventanilla-B"));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(creada.id(), "Ana", "Otro", Prioridad.ALTA));
        servicio.eliminar(creada.id());
    }

    @Test void ejemplosRangoYVisibilidadCompartida() {
        var repo = new RepositorioSolicitudesMemoria();
        var servicio = new ServicioSolicitudes(repo);
        assertThrows(ReglaNegocioException.class, () -> servicio.cargarEjemplos(0));
        assertThrows(ReglaNegocioException.class, () -> servicio.cargarEjemplos(10001));
        assertEquals(10000, servicio.cargarEjemplos(10000));
        assertEquals(10000, repo.listar().size());
        var primera = repo.tomarSiguientePendiente("Ventanilla-A").orElseThrow();
        assertEquals(Prioridad.ALTA, primera.prioridad());
        assertEquals(3, primera.id());
        assertEquals(1, servicio.contar(EstadoSolicitud.EN_PROCESO));
    }

    @Test void atencionConcurrenteSinDuplicados() throws Exception {
        var repo = new RepositorioSolicitudesMemoria();
        var servicio = new ServicioSolicitudes(repo);
        servicio.cargarEjemplos(1000);
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Integer> atender = () -> {
                int total = 0;
                while (true) {
                    var siguiente = repo.tomarSiguientePendiente(Thread.currentThread().getName());
                    if (siguiente.isEmpty()) return total;
                    int id = siguiente.orElseThrow().id();
                    assertTrue(ids.add(id));
                    assertTrue(repo.marcarAtendida(id, Thread.currentThread().getName()));
                    total++;
                }
            };
            var a = executor.submit(atender);
            var b = executor.submit(atender);
            assertEquals(1000, a.get(10, TimeUnit.SECONDS) + b.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1000, servicio.contar(EstadoSolicitud.ATENDIDA));
    }

    @Test void contadoresSegurosYCancelacion() throws Exception {
        for (Contador contador : new Contador[] { new ContadorCompartido(), new ContadorAtomico() }) {
            var a = new Thread(new TareaIncremento(contador, 10000));
            var b = new Thread(new TareaIncremento(contador, 10000));
            a.start(); b.start(); a.join(); b.join();
            assertEquals(20000, contador.getTotal());
            contador.reiniciar();
            assertEquals(0, contador.getTotal());
        }
        var hilo = new Thread(new ProcesoVentanilla("Prueba"));
        hilo.start(); hilo.interrupt(); hilo.join(2000);
        assertFalse(hilo.isAlive());
        assertTrue(hilo.isInterrupted());
    }
}
