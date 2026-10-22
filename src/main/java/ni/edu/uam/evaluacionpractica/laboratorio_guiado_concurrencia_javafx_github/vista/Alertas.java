package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.vista;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/** Único punto de creación de Alert en toda la aplicación. */
public final class Alertas {

    private static volatile Window propietario;

    private Alertas() {
    }

    public static void informacion(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    public static void advertencia(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    public static void error(String titulo, Throwable causa) {
        String detalle;
        if (causa == null) {
            detalle = "Ocurrió un error inesperado.";
        } else {
            String msg = causa.getMessage();
            detalle = (msg == null || msg.isBlank())
                    ? "Causa: " + causa.getClass().getSimpleName()
                    : msg + "\nCausa: " + causa.getClass().getSimpleName();
        }
        mostrar(Alert.AlertType.ERROR, titulo, detalle);
    }

    /** SOLO desde el hilo JavaFX. Devuelve true si el usuario acepta. */
    public static boolean confirmar(String titulo, String mensaje) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Alertas.confirmar solo puede llamarse desde el hilo JavaFX");
        }
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, titulo, mensaje);
        alerta.getButtonTypes().setAll(aceptar, cancelar);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == aceptar;
    }

    public static void establecerPropietario(Window ventana) {
        propietario = ventana;
    }

    // auxiliares privados

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Platform.runLater(() -> crear(tipo, titulo, mensaje).showAndWait());
    }
