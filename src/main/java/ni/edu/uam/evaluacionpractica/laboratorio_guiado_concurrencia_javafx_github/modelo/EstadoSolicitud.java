package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

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
