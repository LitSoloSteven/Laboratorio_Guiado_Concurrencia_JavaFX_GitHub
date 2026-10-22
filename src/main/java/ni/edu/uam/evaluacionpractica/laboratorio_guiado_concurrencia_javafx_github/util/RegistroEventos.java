package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RegistroEventos {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void registrar(TextArea areaRegistro, String mensaje) {
        String linea = String.format("[%s] %s\n", LocalTime.now().format(FORMATO_HORA), mensaje);
        Platform.runLater(() -> areaRegistro.appendText(linea));
    }
}