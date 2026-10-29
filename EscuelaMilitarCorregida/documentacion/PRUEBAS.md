# Pruebas de la versión 1.2

La ejecución y sus resultados reales se detallan en VERIFICACION.md. Los recorridos manuales siguientes son una guía para Android Studio y un teléfono; no significan que se hayan ejecutado aquí en hardware físico.

## Pruebas automatizadas

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug compileDebugAndroidTestKotlin
```

- ValidadoresTest: correo, clave, fechas, anticipación, normas, ocupación, conflictos con eventos, tokens, horarios y días por instalación, filtros de reportes y preferencias por cuenta.
- PersistenciaTest: ejecuta Room y DataStore con Robolectric. Verifica catálogo, exclusión de reservas concurrentes, rechazo sin motivo, cancelación solo de confirmadas, lista de espera, horarios persistidos, notificaciones tipificadas y preferencias. Crea una base con el esquema 1, carga registros, ejecuta la migración real y abre el resultado con Room para validar esquema y datos. También comprueba la migración de una base antigua vacía.
- PantallasTest: ejecuta Compose con Robolectric. Comprueba bloques visibles, ausencia de participantes y motivos, acciones de una pendiente, navegación inferior, evento seleccionado, filtros de instalaciones, reportes y selección de bloques por administración. Guarda capturas en `app/build/capturas-pruebas` para inspección visual.
- RepositorioInstrumentedTest: casos preparados para ejecutarse en emulador o teléfono con `connectedDebugAndroidTest`. La compilación de estos tests se verifica por separado; no equivale a su ejecución en un dispositivo.

La primera ejecución necesita internet para descargar dependencias de pruebas y el Android simulado por Robolectric. Las pruebas locales usan API 28; se recomienda complementar con Android reciente en emulador o teléfono.

## Recorridos manuales sugeridos

| Caso | Acción | Resultado esperado |
| --- | --- | --- |
| Acceso | Entrar con cada cuenta del README | Inicio y menú acordes al rol. |
| Estilo | Abrir inicio, reserva, configuración y reportes | Azul marino, dorado, tarjetas y títulos coherentes. |
| Pantalla pequeña | Usar teléfono compacto y rotación | Menú de una línea; contenido desplazable; sin acciones recortadas. |
| Pantalla amplia | Usar tablet | Navegación lateral y contenido centrado. |
| Tema oscuro | Cambiar en Perfil o Ajustes → Preferencias | Colores legibles; persistencia tras reiniciar. |
| Sin participantes | Recorrer búsqueda, confirmación, detalle, historial y CSV | No hay cantidad de participantes ni motivos de reserva. |
| Calendario del cadete | Abrir fecha de reserva | Pasado y más de 3 días deshabilitados y grises, con política inicial. |
| Otros grados | Repetir con cabo y teniente | Límites iniciales de 7 y 14 días. |
| Hora pasada | Buscar una reserva para hoy | Bloques que ya empezaron deshabilitados. |
| Jornada base | Abrir búsqueda en una instalación nueva | Diez bloques, desde 08:00–09:00 hasta 17:00–18:00. |
| Horario personalizado | Admin cambia días y quita un bloque sin compromisos | El usuario no puede reservar ese día o bloque. |
| Compromisos existentes | Quitar un bloque con una reserva o evento futuro | Se informa el conflicto y no se altera el horario guardado. |
| Catálogo | Contar instalaciones activas en instalación nueva | 20 espacios; seis canchas de tenis separadas. |
| Filtro de estado | Admin combina texto, archivo y estado | Listado y cantidad responden a todos los filtros. |
| Solicitud | Usuario solicita bloque disponible y acepta normas | Pendiente, bloque ocupado y un token usado. |
| Rechazo | Admin abre pendiente y pulsa Rechazar | Rechaza directamente sin escribir motivos; libera el token. |
| Pendiente | Revisar como admin y como titular | No aparece Cancelar reserva. |
| Confirmación | Admin aprueba pendiente | Estado confirmada y notificación al titular. |
| Cancelación | Titular o admin cancela confirmada futura | Bloque libre; token liberado en su semana original. |
| Espera | Dos personas se apuntan al mismo bloque ocupado; cancelar la confirmada | Se asigna por orden a la primera elegible, con política vigente. |
| Reprogramación | Admin cambia una confirmada a otro bloque válido | Conserva semana del token y actualiza bloque y notificación. |
| Navegación inferior | Desde detalle de evento, pasar a Agenda y volver a Eventos | Abre el listado principal, no el detalle anterior. Repetir en cada sección. |
| Títulos | Recorrer pantallas secundarias | Encabezado describe la pantalla actual. |
| Eventos de inicio | Tocar una tarjeta | Abre ese evento, con fecha, horario y lugar. |
| Equipos | Inscribir equipo de la misma compañía | Respeta mínimo de integrantes, cupos y miembros únicos. |
| Tipos de notificación | Cambiar filtro en cada rol | Solo se muestran notificaciones del tipo elegido y de esa cuenta. |
| Preferencias de tipo | Desactivar Eventos | Se ocultan esas notificaciones y reaparecen al activar. |
| Preferencia general | Desactivar todo y luego activar | Se conservan las selecciones de cada tipo. |
| Separación por cuenta | Cambiar preferencias como usuario y entrar como admin | Las elecciones del usuario no modifican las del administrador. |
| Reportes | Cambiar períodos y rango personalizado | Resúmenes y gráficos usan el mismo conjunto de reservas. |
| CSV filtrado | Exportar y abrir el archivo | Contiene solo el período seleccionado y no incluye participantes ni motivos. |
| Sin datos | Elegir un período sin reservas | Mensaje claro, contadores cero y ningún porcentaje inventado. |
| Reinicio | Cerrar proceso y volver a entrar | Room conserva registros y DataStore las preferencias. |
| Actualización | Ejecutar 1.2 sobre una instalación 1.1 sin borrar datos | Migra el esquema, conserva reservas y corrige el catálogo. |
| Foto | Seleccionar una imagen y reiniciar | Foto visible si el proveedor conserva el acceso a su URI. |
| Calendario externo | Agregar confirmada al calendario | Abre el editor del dispositivo; el usuario revisa y guarda. |
| Rol | Entrar como usuario | No dispone de acciones administrativas. |
| Cierre de sesión | Salir y presionar Atrás | No vuelve a la sesión cerrada. |
