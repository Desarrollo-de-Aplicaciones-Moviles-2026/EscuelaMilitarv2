# Cambios de la versión 1.2

Esta versión aplica los 17 ajustes solicitados el 29 de octubre de 2026 sobre el proyecto corregido. Las capturas adjuntas se utilizan como referencia visual. La lógica sigue las indicaciones escritas del estudiante. Se mantienen Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow, Room y DataStore.

## Correspondencia con la lista solicitada

| Punto | Implementación |
| --- | --- |
| 1 | Tema institucional con azul marino, dorado, fondo gris claro, tarjetas blancas y bordes redondeados. Colores, tipografía y formas centralizados en `ui/theme`; componentes consumen `MaterialTheme`. Se conserva el tema oscuro. |
| 2 | Eliminados participantes del formulario, confirmación, detalle, validaciones, lista de espera, exportación CSV y entidades de reservas y espera. La capacidad de una instalación permanece como dato descriptivo; no se solicita tamaño de grupo ni se utiliza para autorizar reservas. |
| 3 | `CampoFecha` utiliza `SelectableDates` de Material 3. En reserva y calendario del usuario se deshabilitan días anteriores a hoy y posteriores al máximo de anticipación de su grado, con extremos inclusivos. Los bloques ya iniciados también se deshabilitan. Crear eventos permite desde hoy; reprogramar respeta el límite del titular. Los reportes permiten fechas históricas porque deben analizar períodos pasados. |
| 4 | Rechazo directo sin formulario de motivos. Eliminados el estado del motivo, su validación y el campo de respuesta de la reserva. La notificación informa el rechazo sin explicaciones escritas. |
| 5 | Instalaciones del administrador permite combinar nombre, activas/archivadas y estado disponible/mantenimiento/no disponible. |
| 6 | Nombre visible unificado como Notificaciones. Filtro por tipo para ambos perfiles. Preferencias generales y por tipo guardadas con DataStore, separadas por cuenta. Tipos: solicitudes, reservas, lista de espera, eventos y competencias, cuenta. Desactivar oculta esas notificaciones dentro de la app y su indicador; conserva los registros para volver a verlos al activar. |
| 7 | La barra inferior navega a la raíz de cada sección sin restaurar una pantalla secundaria guardada. |
| 8 | Etiquetas breves en una línea. Usuario: Inicio, Reservas, Eventos, Agenda, Perfil. Administración: Inicio, Agenda, Eventos, Espacios, Ajustes. Los títulos superiores mantienen nombres completos. |
| 9 | Reportes con próximos 30 días, últimos 7, últimos 30, este mes y período personalizado. Tarjetas resumen, gráfico circular por estado y barras de las cinco instalaciones con más solicitudes. El CSV utiliza exactamente las reservas filtradas. El filtro se aplica a la fecha de la actividad reservada. |
| 10 | Nuevas instalaciones con jornada 08:00–18:00, diez bloques de una hora (último: 17:00–18:00). Administración puede configurar apertura, cierre, días y bloques individuales; cada instalación guarda su selección. Reserva, espera, aprobación, reprogramación y creación de eventos respetan esa configuración. No se permite guardar cambios que invaliden compromisos futuros. |
| 11 | Catálogo de 20 espacios: los 14 espacios individuales de la lista y seis canchas de tenis independientes. Se conservan los IDs de instalaciones que corrigen su nombre para no romper relaciones existentes. Las tres instalaciones retiradas del catálogo original se archivan. |
| 12 | Cancelar solo aparece y funciona para reservas futuras confirmadas, tanto para su titular como para administración. Las pendientes se aprueban o rechazan. La regla también se verifica en el repositorio. |
| 13 | Íconos según disciplina o tipo: atletismo, musculación, básquetbol, vóleibol, tenis, rugby, fútbol, estadio, combate, auditorio, multicancha y pentatlón. |
| 14 | Bloques horarios en una cuadrícula visible, sin menú desplegable. Se incluyen los horarios adicionales que configure administración. Los horarios no habilitados se muestran grises. |
| 15 | Título superior específico en cada ruta: Buscar una instalación, Instalaciones encontradas, Solicitar reserva, Notificaciones, Reportes de reservas, Datos y horarios y demás pantallas. Inicio del usuario muestra su saludo y fecha. |
| 16 | Eliminado el recuadro de motivo u observaciones del usuario y su columna en reservas. Las indicaciones de uso del administrador sobre una instalación se conservan como información de esa instalación. |
| 17 | Inicio muestra Próximos eventos, ordenados cronológicamente. Tarjetas con fecha destacada, nombre, horario, disciplina y lugar. Cada tarjeta abre el detalle de su propio evento y Ver todos abre el listado. |

## Relación con las clases

| Contenido del material adjunto | Aplicación en el código |
| --- | --- |
| Estructura básica del proyecto con MVVM | Pantallas en `ui/screens`, componentes en `ui/components`, entidades y estados en `model`, operaciones en `repository`, estado y acciones en `viewmodel`. |
| Implementación de colores, temas y tipografía y MaterialTheme | `Color.kt`, `Theme.kt`, `Type.kt` y `Shape.kt`; no se copian colores del HTML en cada pantalla. |
| Data class para formularios y errores | ReservaFormulario, CanchaFormulario, sus errores y los demás formularios. Campos retirados eliminados del modelo completo. |
| StateFlow y ViewModel compartido | AppViewModel combina datos y preferencias; CadeteViewModel y AdminViewModel administran formularios y operaciones. El filtro de reportes está en el estado de AdminViewModel. |
| Estado en Compose | Filtros de listados con rememberSaveable; selección de formulario observada desde StateFlow con ciclo de vida. |
| DatePicker y componentes Material 3 | Selección de fecha con días habilitados; FilterChip para bloques y días; Switch para preferencias. |
| SQLite, CRUD y Room | Esquema 2 de Room, consultas del DAO y migración explícita 1 a 2. Operaciones compuestas dentro de transacciones. |
| DataStore | Tema, foto y preferencias generales y por tipo de notificación. |
| Animaciones y estados de carga | Se mantienen indicadores de carga, AnimatedVisibility y color de estados animado. |
| Diseño adaptable | Se conservan Window Size Classes, navegación lateral en anchos mayores y barra inferior compacta. |
| APIs nativas | Se conservan selector de foto, exportación de archivo y apertura del calendario del dispositivo. |

La migración manual, la cuadrícula de bloques y los gráficos son decisiones de implementación para cumplir los cambios. No se presentan como requisitos explícitos del profesor. Los gráficos usan componentes y Canvas de Compose, sin agregar una biblioteca de gráficos a la aplicación.

## Base de datos

Se conserva el archivo local `gestion_deportiva.db`, ahora con esquema Room 2. `DeportesApplication` registra `Migraciones.MIGRACION_1_2`. No se utiliza recreación destructiva. Los archivos JSON de esquema 1 y 2 se conservan para documentar la evolución y probar la migración; el esquema activo es el 2. También se verifica la actualización de bases antiguas vacías.

- `reservas`: conserva id, usuarioId, canchaId, fecha, horaInicio, estado y semanaSolicitud. Elimina participantes, motivo y respuesta.
- `espera`: elimina participantes y conserva orden y bloque solicitado.
- `avisos` pasa a `notificaciones`; conserva registros y agrega tipo. Los rechazos antiguos reciben un mensaje genérico al retirar su motivo.
- `canchas`: agrega diasActivos y bloquesDisponibles. Se guardan como enteros; `Horarios` convierte las selecciones de días y horas a estos valores y viceversa.
- Las otras tablas conservan su estructura: usuarios, politicas, eventos, evento_canchas, equipos y equipo_integrantes.

La migración corrige nombres y añade los espacios nuevos. Rugby N.° 1, Piscina y Muro de escalada se archivan, conservando sus referencias e historial. La antigua entrada de tenis se convierte en Tenis N.° 1 y se añaden las canchas 2 a 6. Fútbol N.° 1 corrige su nombre a Fútbol N.° 2; Sala de Combate pasa a Tatami.

El antiguo valor por defecto de 08:00–20:00 cambia a 08:00–18:00 cuando no existen reservas activas o eventos que necesiten bloques posteriores. Las jornadas personalizadas previas y los compromisos registrados se conservan; administración puede editarlos respetando esas actividades. Una instalación creada con esta versión siempre parte con 08:00–18:00.

Los tokens continúan calculándose por semana de solicitud. Una solicitud pendiente ocupa el bloque y consume un token; rechazarla lo libera. Cancelar una confirmada libera el bloque y su consumo en la semana original. La lista de espera sigue el orden de llegada y vuelve a comprobar las reglas antes de asignar un cupo.

## Archivos para revisar primero

- `ui/theme/`: estilo institucional.
- `ui/components/AppScaffold.kt`: títulos, selección de sección y navegación inferior.
- `ui/components/FormularioComponents.kt`: fechas habilitadas.
- `ui/components/ElementosVisuales.kt`: bloques, fechas compactas, íconos y tarjetas de eventos.
- `ui/screens/NuevaReservaScreen.kt`, `ConfirmarReservaScreen.kt`, `DetalleReservaScreen.kt`: reserva simplificada.
- `ui/screens/EditarCanchaScreen.kt`: días y bloques por instalación.
- `ui/screens/PreferenciasScreen.kt`, `NotificacionesScreen.kt`: configuración y filtros.
- `model/ReportesModel.kt`, `ui/screens/ReportesScreen.kt`: filtro de períodos y gráficos.
- `repository/DeportesRepository.kt`: reglas y transacciones.
- `data/Migraciones.kt`: actualización de bases ya instaladas.

El resultado continúa siendo una aplicación académica local. Cada teléfono tiene su propia base. Las notificaciones son internas; no se añadieron push ni un servidor. Las cuentas de demostración y la contraseña siguen descritas en README.

La evidencia de compilación, pruebas y sus límites se encuentra en `VERIFICACION.md`.
