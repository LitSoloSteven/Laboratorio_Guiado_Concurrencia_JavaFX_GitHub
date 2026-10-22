package ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RegistroEventos {

    private final BlockingQueue<String> mensajes = new LinkedBlockingQueue<>();

    public RegistroEventos() {
    }

    public void publicar(String mensaje) {
        Objects.requireNonNull(mensaje, "mensaje no puede ser null");
        mensajes.add(mensaje);
    }

    public List<String> vaciar() {
        List<String> lote = new ArrayList<>();
        mensajes.drainTo(lote);
        return lote;
    }
}
