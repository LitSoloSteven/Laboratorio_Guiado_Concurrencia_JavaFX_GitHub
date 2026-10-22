# Estación 3: sincronización y repositorio concurrente

## Predicción

Dos hilos con 1000 incrementos cada uno deben producir 2000. `total++` lee, suma y escribe: sin exclusión mutua puede perder incrementos. El resultado inseguro también puede ser 2000 en algunas ejecuciones. `synchronized` y `AtomicInteger` deben producir 2000 en todas las ejecuciones sin interrupciones.

El repositorio debe elegir primero ALTA, después MEDIA y por último BAJA. Dentro de cada prioridad debe elegir el menor id. Tomar y marcar EN_PROCESO debe ser una operación indivisible para que dos ventanillas no obtengan la misma solicitud.

## Revisión del trabajo existente

Se conservaron los tres contadores: sin protección para la demostración, métodos sincronizados en `ContadorCompartido` y operaciones atómicas en `ContadorAtomico`. `TareaIncremento` valida sus parámetros y consulta la interrupción antes de incrementar. `RegistroEventos` usa una cola concurrente y extrae los mensajes por lotes.

El repositorio protege todas sus operaciones públicas con el mismo monitor. Usa un `LinkedHashMap` para conservar el orden por id y un `TreeSet` por prioridad para localizar pendientes sin recorrer toda la colección. Al editar una prioridad, eliminar o devolver una solicitud a pendiente actualiza también ese índice. Devuelve snapshots inmutables de records `Solicitud`.

Se completaron `DemoCondicionCarrera` y `DemoRepositorioConcurrente`, que estaban vacías. La primera usa una señal común de inicio para favorecer la competencia entre los dos hilos. Esto no garantiza una pérdida en la versión insegura. La segunda comprueba ids únicos, estados finales y contador; propaga las excepciones de los hilos al hilo principal para no informar un éxito falso.

## Resultados observados

Ejecución real con JDK 21, dos hilos y 1000 incrementos por hilo:

| Repetición | Sin sincronizar | synchronized | AtomicInteger |
| --- | --- | --- | --- |
| 1 | 1595 | 2000 | 2000 |
| 2 | 2000 | 2000 | 2000 |
| 3 | 1999 | 2000 | 2000 |
| 4 | 2000 | 2000 | 2000 |
| 5 | 2000 | 2000 | 2000 |
| 6 | 1993 | 2000 | 2000 |
| 7 | 2000 | 2000 | 2000 |
| 8 | 1977 | 2000 | 2000 |
| 9 | 2000 | 2000 | 2000 |
| 10 | 1789 | 2000 | 2000 |

La versión insegura perdió incrementos en 5 de 10 ejecuciones. Las otras dos obtuvieron 2000 en todas las ejecuciones. La salida completa está en [i3-contadores-salida.txt](capturas/i3-contadores-salida.txt). Estos resultados son una observación de esta ejecución; el orden y las pérdidas del contador inseguro pueden variar.

La demo del repositorio se ejecutó con 0, 1, 1000 y 10000 solicitudes. Cada lote terminó con tantos ids únicos y solicitudes atendidas como solicitudes creadas y sin duplicados. Evidencia: [i3-repositorio-salida.txt](capturas/i3-repositorio-salida.txt).

## Pruebas automatizadas

`ConcurrenciaIntegrante3Test` cubre prioridad y desempate por id, cambio de prioridad, eliminación sin residuos en el índice, snapshots inmutables, estados bloqueados, cancelación, creación concurrente con 4000 ids únicos, publicación y vaciado concurrente de 4000 mensajes, validación e interrupción de la tarea, y cancelación y reinicio de una sesión. Se conservan además las pruebas del CRUD, de los contadores seguros y de la carga del FXML.

Resultado: 14 pruebas, sin fallos ni errores. La prueba de cancelación confirma que no queden solicitudes EN_PROCESO y que el contador de la nueva sesión coincida con las atenciones de sus dos ventanillas.

## Integración y conflictos

Se resolvió el conflicto de `Alertas.java` producido al restaurar el stash, conservando la implementación de main. Se guardó un respaldo en stash y se llevaron los cambios a `feature/StevenCuadra`, incorporando los commits de main y Caleb. Se conservaron los cambios recuperados del servicio, modelos y controlador para usar un único repositorio compartido.

Para compilar con el monitor ya incorporado en main se corrigieron sus imports y se completaron las dependencias vacías `EstadoSesion`, `Ventanilla` y `GestorVentanillas`, junto con la fábrica de `ContextoApp`. El gestor realiza los join en `Supervisor-Sesion`, conserva los nombres acordados y no bloquea al iniciar o detener. La integración visual completa de las otras pantallas y el arranque general de la aplicación siguen fuera de esta entrega.

## Reproducción en PowerShell

Con JAVA_HOME configurado para JDK 21:

```powershell
.\mvnw.cmd clean test
& "$env:JAVA_HOME/bin/java.exe" -cp target/classes ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo.DemoCondicionCarrera
& "$env:JAVA_HOME/bin/java.exe" -cp target/classes ni.edu.uam.evaluacionpractica.laboratorio_guiado_concurrencia_javafx_github.demo.DemoRepositorioConcurrente 10000
```

## Análisis

`join()` permite leer el resultado cuando ambos hilos han terminado, pero no evita los incrementos perdidos durante su ejecución. La protección debe estar en la operación compartida. El contador sincronizado usa el mismo monitor para incrementar, consultar y reiniciar; el atómico ofrece esas operaciones mediante `AtomicInteger`.

Los controles JavaFX deben seguir modificándose desde su propio hilo. Proteger el repositorio o el contador no autoriza a actualizar controles desde las ventanillas. La cola de eventos y el actualizador periódico mantienen esa separación.

El índice de pendientes evita buscar sobre todas las solicitudes al tomar la siguiente: cada extracción cuesta O(log n) dentro de su prioridad. El monitor único simplifica las garantías de consistencia, aunque serializa las operaciones. Para escalar conviene medir la carga real antes de dividir los bloqueos, porque la selección y el cambio de estado deben seguir siendo atómicos.
