package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.app;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.Contador;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.servicio.ServicioSolicitudes;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.tareas.GestorVentanillas;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util.RegistroEventos;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista.ActualizadorPeriodico;

import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.repositorio.RepositorioSolicitudesMemoria;

public record ContextoApp(ServicioSolicitudes servicio, Contador contador,
                          RegistroEventos registro, GestorVentanillas gestor,
                          ActualizadorPeriodico actualizador) {
    public static ContextoApp crear() {
        var repositorio = new RepositorioSolicitudesMemoria();
        var contador = new ContadorCompartido();
        var registro = new RegistroEventos();
        return new ContextoApp(new ServicioSolicitudes(repositorio), contador, registro,
                new GestorVentanillas(repositorio, contador, registro::publicar),
                new ActualizadorPeriodico(ActualizadorPeriodico.INTERVALO_POR_DEFECTO_MS));
    }
}
