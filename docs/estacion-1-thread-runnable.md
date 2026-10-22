# Estación 1: Thread y Runnable

## Alcance

Cambios de `feature/caleb` integrados en `feature/StevenCuadra` mediante el commit `03675c7`. Se completaron el servicio CRUD, su controlador, la demo por consola y las dependencias necesarias para compilar e inyectar el contexto compartido. Se conserva el diseño FXML aportado por Caleb.

## Predicción y comportamiento

Al invocar `run()` directamente, ambas tareas se ejecutan en el hilo `main`: primero los cinco pasos de A y después los cinco de B. Con `start()`, las tareas se ejecutan en `Ventanilla-A` y `Ventanilla-B`; cada tarea conserva su orden interno, pero el orden entre tareas depende del planificador. Ambas usan cinco pausas de 500 ms. `join()` solo se utiliza en la demostración por consola.

Ante una interrupción durante `sleep()`, la tarea restaura la bandera de interrupción y termina. También consulta la bandera antes de cada iteración.

## Evidencia reproducible

Se ejecutó `DemoThreadRunnable` tres veces con JDK 21. La salida completa está en [i1-thread-runnable-salida.txt](capturas/i1-thread-runnable-salida.txt). Las ejecuciones muestran `main` para la variante secuencial y los nombres de ambas ventanillas para la concurrente. La alternancia concreta se consulta en esa salida; no se garantiza un orden entre hilos.

Desde PowerShell, con `JAVA_HOME` apuntando al JDK 21:

```powershell
.\mvnw.cmd test
& "$env:JAVA_HOME/bin/java.exe" -cp target/classes ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo.DemoThreadRunnable
```

## CRUD e integración

`ServicioSolicitudes` recibe `RepositorioSolicitudes` por constructor. Las reglas de longitud, obligatoriedad, prioridad y cantidad de ejemplos se aplican antes de almacenar datos. Las operaciones de actualización y eliminación traducen los resultados del repositorio a `ReglaNegocioException`. Las decisiones sobre estados se hacen dentro de las operaciones sincronizadas del repositorio.

`Solicitud` usa los enums públicos `Prioridad` y `EstadoSolicitud`, compartidos por el servicio y el repositorio. El controlador obtiene el servicio desde `ContextoApp`, registra `refrescarTabla()` en el actualizador y conserva la selección por id y los cambios del formulario durante los refrescos. Las alertas se crean en `Alertas`.

## Verificación

`IntegracionSolicitudesTest` comprueba el CRUD, snapshots inmutables, validaciones, límites de ejemplos, estados bloqueados, cancelación y atención concurrente de 1000 solicitudes sin ids duplicados. Comprueba además los contadores sincronizado y atómico y la interrupción de `ProcesoVentanilla`.

`SolicitudesFxmlTest` inicia JavaFX, carga el FXML real, inyecta el contexto, refresca la tabla, verifica que no pierde una edición y ejecuta el botón Limpiar. Las pruebas automatizadas no sustituyen la comprobación manual de los diálogos y de la aplicación completa.

La integración de las pantallas de ventanillas e hilos, el arranque principal y el ciclo completo de sesión corresponden a otros integrantes y siguen pendientes en el esqueleto de esta rama. Esta entrega valida el módulo de solicitudes y la estación 1.
