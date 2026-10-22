package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ActualizadorPeriodico {
    public static final long INTERVALO_POR_DEFECTO_MS = 250;
    private final List<Runnable> tareas = new ArrayList<>();
    private final Timeline timeline;

    public ActualizadorPeriodico(long intervaloMs) {
        if (intervaloMs <= 0) throw new IllegalArgumentException("El intervalo debe ser positivo.");
        timeline = new Timeline(new KeyFrame(Duration.millis(intervaloMs), e -> ejecutar()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void agregarTarea(Runnable tarea) { tareas.add(Objects.requireNonNull(tarea)); }
    public void iniciar() { timeline.play(); }
    public void detener() { timeline.stop(); ejecutar(); }
    private void ejecutar() { List.copyOf(tareas).forEach(Runnable::run); }
}
