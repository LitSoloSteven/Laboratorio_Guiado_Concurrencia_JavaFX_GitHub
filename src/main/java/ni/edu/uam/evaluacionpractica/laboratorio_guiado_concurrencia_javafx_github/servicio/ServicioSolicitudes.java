package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Prioridad;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudes;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.ResultadoOperacion;

public class ServicioSolicitudes {
    public static final int MAX_LONGITUD_SOLICITANTE = 40;
    public static final int MAX_LONGITUD_ASUNTO = 80;
    public static final int MAX_EJEMPLOS = 10_000;
    private final RepositorioSolicitudes repositorio;

    public ServicioSolicitudes(RepositorioSolicitudes repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

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

    public Solicitud crear(String solicitante, String asunto, Prioridad prioridad) {
        validarCampos(solicitante, asunto, prioridad);
        return repositorio.crear(Solicitud.nueva(solicitante.trim(), asunto.trim(), prioridad));
    }

    public Optional<Solicitud> buscar(int id) { return repositorio.buscarPorId(id); }
    public List<Solicitud> listar() { return repositorio.listar(); }
    public int contar(EstadoSolicitud estado) { return repositorio.contarPorEstado(estado); }

    public Solicitud actualizar(int id, String solicitante, String asunto, Prioridad prioridad) {
        validarCampos(solicitante, asunto, prioridad);
        Solicitud actual = buscar(id).orElseThrow(() -> new ReglaNegocioException("La solicitud no existe."));
        Solicitud modificada = actual.conDatos(solicitante.trim(), asunto.trim(), prioridad);
        comprobar(repositorio.actualizar(modificada), "Solo se pueden editar solicitudes pendientes.");
        return modificada;
    }

    public void eliminar(int id) {
        comprobar(repositorio.eliminar(id), "No se puede eliminar una solicitud en proceso.");
    }

    private void comprobar(ResultadoOperacion resultado, String mensajeBloqueo) {
        switch (resultado) {
            case NO_ENCONTRADA -> throw new ReglaNegocioException("La solicitud no existe.");
            case BLOQUEADA -> throw new ReglaNegocioException(mensajeBloqueo);
            case OK -> { }
        }
    }

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

}
