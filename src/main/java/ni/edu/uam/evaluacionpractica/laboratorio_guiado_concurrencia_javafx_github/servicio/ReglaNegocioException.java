package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio;

/**
 * Excepción personalizada para validaciones y reglas de negocio del CRUD.
 * El mensaje contenido se utiliza directamente para mostrar alertas al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    /**
     * Constructor con el mensaje descriptivo del fallo de regla de negocio.
     *
     * @param mensaje Detalle del error o validación no superada.
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
