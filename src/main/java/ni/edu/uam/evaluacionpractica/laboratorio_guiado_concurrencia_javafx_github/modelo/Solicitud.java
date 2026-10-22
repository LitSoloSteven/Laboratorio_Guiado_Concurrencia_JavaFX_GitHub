package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

import java.time.LocalDateTime;

public record Solicitud(
        int id,
        String solicitante,
        String asunto,
        Prioridad prioridad,
        EstadoSolicitud estado,
        String atendidaPor,
        LocalDateTime creadaEn
) {

    public static Solicitud nueva(String solicitante, String asunto, Prioridad prioridad) {
        return new Solicitud(0, solicitante, asunto, prioridad, EstadoSolicitud.PENDIENTE, null, LocalDateTime.now());
    }

    public Solicitud conId(int nuevoId) {
        return new Solicitud(nuevoId, this.solicitante, this.asunto, this.prioridad, this.estado, this.atendidaPor, this.creadaEn);
    }

    public Solicitud conDatos(String nuevoSolicitante, String nuevoAsunto, Prioridad nuevaPrioridad) {
        return new Solicitud(this.id, nuevoSolicitante, nuevoAsunto, nuevaPrioridad, this.estado, this.atendidaPor, this.creadaEn);
    }

    public Solicitud conEstado(EstadoSolicitud nuevoEstado, String nuevaAtendidaPor) {
        return new Solicitud(this.id, this.solicitante, this.asunto, this.prioridad, nuevoEstado, nuevaAtendidaPor, this.creadaEn);
    }

    public int getId() { return id; }
    public String getSolicitante() { return solicitante; }
    public String getAsunto() { return asunto; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoSolicitud getEstado() { return estado; }
    public String getAtendidaPor() { return atendidaPor; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}
