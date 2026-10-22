package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio;

import java.util.List;
import java.util.Optional;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.EstadoSolicitud;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Solicitud;

public interface RepositorioSolicitudes {
    Solicitud crear(Solicitud nueva);
    Optional<Solicitud> buscarPorId(int id);
    List<Solicitud> listar();
    ResultadoOperacion actualizar(Solicitud modificada);
    ResultadoOperacion eliminar(int id);
    int contarPorEstado(EstadoSolicitud estado);
    Optional<Solicitud> tomarSiguientePendiente(String ventanilla);
    boolean marcarAtendida(int id, String ventanilla);
    boolean devolverAPendiente(int id);
}
