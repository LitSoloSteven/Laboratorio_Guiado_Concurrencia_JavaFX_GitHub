package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

import java.time.LocalDateTime;

/**
 * Modelo inmutable que representa una solicitud en el sistema.
 * Al ser un record inmutable, es seguro compartirlo entre diferentes hilos.
 */
public record Solicitud(
        int id,
        String solicitante,
        String asunto,
        Prioridad prioridad,
        EstadoSolicitud estado,
        String atendidaPor,
        LocalDateTime creadaEn
) {

    /**
     * Niveles de prioridad disponibles para una solicitud.
     */
    public enum Prioridad {
        BAJA("Baja"),
        MEDIA("Media"),
        ALTA("Alta");

        private final String etiqueta;

        Prioridad(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    /**
     * Estados por los que transita una solicitud en su ciclo de vida.
     */
    public enum EstadoSolicitud {
        PENDIENTE("Pendiente"),
        EN_PROCESO("En proceso"),
        ATENDIDA("Atendida");

        private final String etiqueta;

        EstadoSolicitud(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    /**
     * Crea una nueva solicitud con ID temporal 0, estado PENDIENTE y fecha y hora actual.
     */
    public static Solicitud nueva(String solicitante, String asunto, Prioridad prioridad) {
        return new Solicitud(0, solicitante, asunto, prioridad, EstadoSolicitud.PENDIENTE, null, LocalDateTime.now());
    }

    /**
     * Genera una nueva instancia copiando los datos actuales pero asignando un nuevo ID.
     */
    public Solicitud conId(int nuevoId) {
        return new Solicitud(nuevoId, this.solicitante, this.asunto, this.prioridad, this.estado, this.atendidaPor, this.creadaEn);
    }

    /**
     * Genera una nueva instancia con los datos modificados (solicitante, asunto y prioridad).
     */
    public Solicitud conDatos(String nuevoSolicitante, String nuevoAsunto, Prioridad nuevaPrioridad) {
        return new Solicitud(this.id, nuevoSolicitante, nuevoAsunto, nuevaPrioridad, this.estado, this.atendidaPor, this.creadaEn);
    }

    /**
     * Genera una nueva instancia actualizando el estado y la ventanilla que atendió la solicitud.
     */
    public Solicitud conEstado(EstadoSolicitud nuevoEstado, String nuevaAtendidaPor) {
        return new Solicitud(this.id, this.solicitante, this.asunto, this.prioridad, nuevoEstado, nuevaAtendidaPor, this.creadaEn);
    }

    // Métodos de acceso tipo getter adicionales para máxima compatibilidad con componentes JavaFX
    public int getId() { return id; }
    public String getSolicitante() { return solicitante; }
    public String getAsunto() { return asunto; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoSolicitud getEstado() { return estado; }
    public String getAtendidaPor() { return atendidaPor; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}
