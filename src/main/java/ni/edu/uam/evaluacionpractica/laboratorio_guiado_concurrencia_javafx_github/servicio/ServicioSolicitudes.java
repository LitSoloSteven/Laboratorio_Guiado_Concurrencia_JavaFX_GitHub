package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud.EstadoSolicitud;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio encargado de la lógica de negocio y validación del CRUD de solicitudes.
 * Asegura la integridad de los datos y provee acceso concurrente seguro (thread-safe).
 */
public class ServicioSolicitudes {

    // Constantes de validación definidas por contrato
    public static final int MAX_LONGITUD_SOLICITANTE = 40;
    public static final int MAX_LONGITUD_ASUNTO = 80;
    public static final int MAX_EJEMPLOS = 10_000;

    // Almacén en memoria de solicitudes protegido para concurrencia
    private final List<Solicitud> solicitudes = new ArrayList<>();

    // Generador atómico de identificadores únicos
    private final AtomicInteger generadorId = new AtomicInteger(0);

    /**
     * Constructor por defecto del servicio.
     */
    public ServicioSolicitudes() {
    }

    /**
     * Valida los campos principales de una solicitud según las reglas de negocio.
     */
    private void validarCampos(String solicitante, String asunto, Prioridad prioridad) {
        if (solicitante == null || solicitante.trim().isEmpty()) {
            throw new ReglaNegocioException("El solicitante es obligatorio.");
        }
        if (solicitante.trim().length() > MAX_LONGITUD_SOLICITANTE) {
            throw new ReglaNegocioException("El solicitante supera la longitud máxima permitida (" + MAX_LONGITUD_SOLICITANTE + " caracteres).");
        }
        if (asunto == null || asunto.trim().isEmpty()) {
            throw new ReglaNegocioException("El asunto es obligatorio.");
        }
        if (asunto.trim().length() > MAX_LONGITUD_ASUNTO) {
            throw new ReglaNegocioException("El asunto supera la longitud máxima permitida (" + MAX_LONGITUD_ASUNTO + " caracteres).");
        }
        if (prioridad == null) {
            throw new ReglaNegocioException("Debe seleccionar una prioridad.");
        }
    }

    /**
     * Crea una nueva solicitud en estado PENDIENTE tras validar sus datos.
     */
    public synchronized Solicitud crear(String solicitante, String asunto, Prioridad prioridad) {
        validarCampos(solicitante, asunto, prioridad);

        int nuevoId = generadorId.incrementAndGet();
        Solicitud nueva = new Solicitud(
                nuevoId,
                solicitante.trim(),
                asunto.trim(),
                prioridad,
                EstadoSolicitud.PENDIENTE,
                null,
                LocalDateTime.now()
        );

        solicitudes.add(nueva);
        return nueva;
    }

    /**
     * Busca una solicitud por su ID.
     */
    public synchronized Optional<Solicitud> buscar(int id) {
        return solicitudes.stream()
                .filter(s -> s.id() == id)
                .findFirst();
    }

    /**
     * Obtiene una copia inmutable (snapshot) de todas las solicitudes ordenadas por ID.
     */
    public synchronized List<Solicitud> listar() {
        return solicitudes.stream()
                .sorted(Comparator.comparingInt(Solicitud::id))
                .toList();
    }

    /**
     * Actualiza una solicitud existente. Solo se permite actualizar si está en estado PENDIENTE.
     */
    public synchronized Solicitud actualizar(int id, String solicitante, String asunto, Prioridad prioridad) {
        validarCampos(solicitante, asunto, prioridad);

        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud actual = solicitudes.get(i);
            if (actual.id() == id) {
                // Validación de regla: solo modificar solicitudes PENDIENTES
                if (actual.estado() != EstadoSolicitud.PENDIENTE) {
                    throw new ReglaNegocioException("No se puede editar: la solicitud está " + actual.estado().getEtiqueta() + ".");
                }

                Solicitud modificada = actual.conDatos(solicitante.trim(), asunto.trim(), prioridad);
                solicitudes.set(i, modificada);
                return modificada;
            }
        }

        throw new ReglaNegocioException("La solicitud con ID " + id + " no fue encontrada.");
    }

    /**
     * Elimina una solicitud del sistema. No se permite eliminar solicitudes EN PROCESO.
     */
    public synchronized void eliminar(int id) {
        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud actual = solicitudes.get(i);
            if (actual.id() == id) {
                // Validación de regla: no eliminar solicitudes en proceso de atención
                if (actual.estado() == EstadoSolicitud.EN_PROCESO) {
                    throw new ReglaNegocioException("No se puede eliminar una solicitud que está En proceso.");
                }

                solicitudes.remove(i);
                return;
            }
        }

        throw new ReglaNegocioException("La solicitud con ID " + id + " no fue encontrada.");
    }

    /**
     * Cuenta la cantidad de solicitudes en un estado específico.
     */
    public synchronized int contar(EstadoSolicitud estado) {
        return (int) solicitudes.stream()
                .filter(s -> s.estado() == estado)
                .count();
    }

    /**
     * Carga un lote de solicitudes de ejemplo para pruebas.
     */
    public synchronized int cargarEjemplos(int cantidad) {
        if (cantidad < 1 || cantidad > MAX_EJEMPLOS) {
            throw new ReglaNegocioException("La cantidad debe estar entre 1 y " + MAX_EJEMPLOS + ".");
        }

        String[] nombresEjemplo = {"Caleb Tardencilla", "Steven Cuadra", "Alfredo Montalvan", "Maria Lopez", "Carlos Ruiz"};
        String[] asuntosEjemplo = {"Emisión de certificado académico", "Solicitud de retiro de asignatura", "Revisión de calificación final", "Trámite de carnet estudiantil", "Constancia de matrícula semestral"};
        Prioridad[] prioridades = Prioridad.values();

        for (int i = 0; i < cantidad; i++) {
            String solicitante = nombresEjemplo[i % nombresEjemplo.length] + " (" + (i + 1) + ")";
            String asunto = asuntosEjemplo[i % asuntosEjemplo.length];
            Prioridad prioridad = prioridades[i % prioridades.length];

            crear(solicitante, asunto, prioridad);
        }

        return cantidad;
    }

    /**
     * Selecciona la siguiente solicitud pendiente con mayor prioridad (ALTA > MEDIA > BAJA)
     * y menor ID, marcándola atómicamente como EN_PROCESO por la ventanilla solicitante.
     */
    public synchronized Optional<Solicitud> tomarSiguientePendiente(String ventanilla) {
        Optional<Solicitud> candidata = solicitudes.stream()
                .filter(s -> s.estado() == EstadoSolicitud.PENDIENTE)
                .sorted(Comparator
                        .comparing((Solicitud s) -> s.prioridad(), Comparator.reverseOrder())
                        .thenComparingInt(Solicitud::id))
                .findFirst();

        if (candidata.isPresent()) {
            Solicitud elegida = candidata.get();
            Solicitud enProceso = elegida.conEstado(EstadoSolicitud.EN_PROCESO, ventanilla);

            // Reemplazamos en la lista
            for (int i = 0; i < solicitudes.size(); i++) {
                if (solicitudes.get(i).id() == elegida.id()) {
                    solicitudes.set(i, enProceso);
                    break;
                }
            }
            return Optional.of(enProceso);
        }

        return Optional.empty();
    }

    /**
     * Marca una solicitud EN_PROCESO como ATENDIDA.
     */
    public synchronized boolean marcarAtendida(int id, String ventanilla) {
        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud s = solicitudes.get(i);
            if (s.id() == id && s.estado() == EstadoSolicitud.EN_PROCESO) {
                solicitudes.set(i, s.conEstado(EstadoSolicitud.ATENDIDA, ventanilla));
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve una solicitud EN_PROCESO nuevamente al estado PENDIENTE (cancelación segura).
     */
    public synchronized boolean devolverAPendiente(int id) {
        for (int i = 0; i < solicitudes.size(); i++) {
            Solicitud s = solicitudes.get(i);
            if (s.id() == id && s.estado() == EstadoSolicitud.EN_PROCESO) {
                solicitudes.set(i, s.conEstado(EstadoSolicitud.PENDIENTE, null));
                return true;
            }
        }
        return false;
    }
}
