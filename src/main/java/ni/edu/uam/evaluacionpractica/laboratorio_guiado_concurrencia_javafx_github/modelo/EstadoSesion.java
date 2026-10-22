package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo;

public enum EstadoSesion {
    EN_ESPERA("En espera"), EJECUTANDO("Ejecutando"), DETENIENDO("Deteniendo"),
    DETENIDO("Detenido"), FINALIZADO("Finalizado");
    private final String etiqueta;
    EstadoSesion(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}
