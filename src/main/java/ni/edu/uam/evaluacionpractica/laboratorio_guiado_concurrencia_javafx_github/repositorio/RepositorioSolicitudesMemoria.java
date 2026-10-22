package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class RepositorioSolicitudesMemoria implements RepositorioSolicitudes {

    private final Map<Integer, Solicitud> solicitudes = new LinkedHashMap<>();
    private final Map<Prioridad, TreeSet<Integer>> pendientesPorPrioridad = new EnumMap<>(Prioridad.class);
    private int siguienteId = 1;

    public RepositorioSolicitudesMemoria() {
        for (Prioridad prioridad : Prioridad.values()) {
            pendientesPorPrioridad.put(prioridad, new TreeSet<>());
        }
    }

    @Override
    public synchronized Solicitud crear(Solicitud nueva) {
        Objects.requireNonNull(nueva, "nueva no puede ser null");
        Objects.requireNonNull(nueva.prioridad(), "la prioridad es obligatoria");

        int id = siguienteId++;
        Solicitud guardada = nueva.conId(id).conEstado(EstadoSolicitud.PENDIENTE, null);
        solicitudes.put(id, guardada);
        pendientesPorPrioridad.get(guardada.prioridad()).add(id);
        return guardada;
    }

    @Override
    public synchronized Optional<Solicitud> buscarPorId(int id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public synchronized List<Solicitud> listar() {
        return List.copyOf(solicitudes.values());
    }

    @Override
    public synchronized ResultadoOperacion actualizar(Solicitud modificada) {
        Objects.requireNonNull(modificada, "modificada no puede ser null");
        Objects.requireNonNull(modificada.prioridad(), "la prioridad es obligatoria");

        Solicitud actual = solicitudes.get(modificada.id());
        if (actual == null) {
            return ResultadoOperacion.NO_ENCONTRADA;
        }
        if (actual.estado() != EstadoSolicitud.PENDIENTE) {
            return ResultadoOperacion.BLOQUEADA;
        }

        if (actual.prioridad() != modificada.prioridad()) {
            pendientesPorPrioridad.get(actual.prioridad()).remove(actual.id());
            pendientesPorPrioridad.get(modificada.prioridad()).add(actual.id());
        }
        solicitudes.put(actual.id(),
                actual.conDatos(modificada.solicitante(), modificada.asunto(), modificada.prioridad()));
        return ResultadoOperacion.OK;
    }

    @Override
    public synchronized ResultadoOperacion eliminar(int id) {
        Solicitud actual = solicitudes.get(id);
        if (actual == null) {
            return ResultadoOperacion.NO_ENCONTRADA;
        }
        if (actual.estado() == EstadoSolicitud.EN_PROCESO) {
            return ResultadoOperacion.BLOQUEADA;
        }

        if (actual.estado() == EstadoSolicitud.PENDIENTE) {
            pendientesPorPrioridad.get(actual.prioridad()).remove(id);
        }
        solicitudes.remove(id);
        return ResultadoOperacion.OK;
    }

    @Override
    public synchronized int contarPorEstado(EstadoSolicitud estado) {
        int cuenta = 0;
        for (Solicitud solicitud : solicitudes.values()) {
            if (solicitud.estado() == estado) {
                cuenta++;
            }
        }
        return cuenta;
    }

    @Override
    public synchronized Optional<Solicitud> tomarSiguientePendiente(String ventanilla) {
        Objects.requireNonNull(ventanilla, "ventanilla no puede ser null");

        Prioridad[] prioridades = Prioridad.values();
        for (int i = prioridades.length - 1; i >= 0; i--) {
            TreeSet<Integer> pendientes = pendientesPorPrioridad.get(prioridades[i]);
            if (!pendientes.isEmpty()) {
                int id = pendientes.pollFirst();
                Solicitud enProceso = solicitudes.get(id).conEstado(EstadoSolicitud.EN_PROCESO, ventanilla);
                solicitudes.put(id, enProceso);
                return Optional.of(enProceso);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean marcarAtendida(int id, String ventanilla) {
        Objects.requireNonNull(ventanilla, "ventanilla no puede ser null");

        Solicitud actual = solicitudes.get(id);
        if (actual == null || actual.estado() != EstadoSolicitud.EN_PROCESO) {
            return false;
        }
        solicitudes.put(id, actual.conEstado(EstadoSolicitud.ATENDIDA, ventanilla));
        return true;
    }

    @Override
    public synchronized boolean devolverAPendiente(int id) {
        Solicitud actual = solicitudes.get(id);
        if (actual == null || actual.estado() != EstadoSolicitud.EN_PROCESO) {
            return false;
        }
        solicitudes.put(id, actual.conEstado(EstadoSolicitud.PENDIENTE, null));
        pendientesPorPrioridad.get(actual.prioridad()).add(id);
        return true;
    }
}
