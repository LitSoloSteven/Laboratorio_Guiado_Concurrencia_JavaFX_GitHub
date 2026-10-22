package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.modelo.ContadorCompartido;

public class ActualizadorPeriodico extends AnimationTimer {
    private final Label etiquetaContador;
    private final ContadorCompartido contador;
    private long ultimaActualizacion = 0;

    public ActualizadorPeriodico(Label etiquetaContador, ContadorCompartido contador) {
        this.etiquetaContador = etiquetaContador;
        this.contador = contador;
    }

    @Override
    public void handle(long now) {
        // Actualiza la interfaz gráfica aproximadamente cada 100ms (100,000,000 nanosegundos)
        if (now - ultimaActualizacion >= 100_000_000) {
            etiquetaContador.setText(String.valueOf(contador.getTotal()));
            ultimaActualizacion = now;
        }
    }
}