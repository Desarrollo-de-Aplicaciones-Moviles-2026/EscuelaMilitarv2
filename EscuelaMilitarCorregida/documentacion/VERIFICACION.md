# Verificación de la versión 1.2

Comprobación realizada el 29 de octubre de 2026 con JDK 17, Gradle 8.9, Android SDK Platform 35 y Build Tools 34.0.0.

| Comprobación | Resultado |
| --- | --- |
| Compilación Android assembleDebug | Correcta; APK de depuración generado |
| Room con KSP | Esquema 2, diez tablas; esquemas 1 y 2 incluidos |
| ValidadoresTest | 19 pruebas aprobadas |
| PersistenciaTest con Robolectric | 9 pruebas aprobadas |
| PantallasTest con Compose y Robolectric | 7 pruebas aprobadas |
| Total de pruebas locales | 35 ejecutadas; 0 fallos, 0 errores y 0 omitidas |
| Migración de una base antigua con datos | Ejecutada y validada por Room; conserva reservas, espera y notificaciones y actualiza catálogo |
| Migración de una base antigua vacía | Ejecutada; la inicialización posterior completa el catálogo |
| Lint | 0 errores; 1 advertencia de actualización disponible para AndroidX Test JUnit 1.2.1 a 1.3.0 |
| Compilación de pruebas instrumentadas | Correcta |
| Revisión visual | Capturas de Compose generadas con Robolectric, incluyendo inicio, búsqueda, solicitud pendiente, horarios y reportes |
| Ejecución en teléfono físico o emulador Android | Pendiente; no ejecutada en este entorno |

Se ejecutaron las tareas:

```bash
./gradlew testDebugUnitTest compileDebugAndroidTestKotlin assembleDebug lintDebug
```

Robolectric ejecutó las pruebas con Android API 28 simulado y gráficos nativos. La prueba de migración abre una base creada con el esquema 1 y la actualiza con la misma migración registrada en la aplicación. La apertura final de Room valida el esquema 2, además de las comprobaciones de datos.

Las capturas se inspeccionaron para comprobar el estilo, textos y distribución de las pantallas probadas. No sustituyen un recorrido completo en hardware físico, ni verifican la apariencia en todos los tamaños, escalas de letra y versiones de Android. El selector de foto, el proveedor de archivos del CSV y el calendario externo deben comprobarse en el teléfono según PRUEBAS.md.

La advertencia de Lint corresponde a una dependencia de pruebas ya presente en la versión anterior. Se mantiene la versión compatible de la base del proyecto; no afecta la ejecución de la aplicación. Las dependencias de pruebas añadidas están centralizadas en el catálogo de versiones.

La comprobación local desactivó el seguimiento de archivos y el agente de instrumentación de Gradle, y ejecutó el compilador Kotlin en el proceso de Gradle. Son ajustes de este entorno, sin modificaciones en las reglas de la app. No se incluyen rutas del SDK ni herramientas descargadas en la entrega.
