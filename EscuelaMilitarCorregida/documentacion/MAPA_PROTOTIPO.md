# Mapa de pantallas de la versión 1.2

Las capturas y el HTML se utilizan como referencia de presentación. Las reglas vigentes son las indicaciones escritas y el informe de cambios.

| Grupo | Pantallas o componentes |
| --- | --- |
| Acceso | LoginScreen |
| Inicio del usuario | CadeteScreen, tarjetas de tokens, próxima reserva y próximos eventos |
| Nueva reserva | NuevaReservaScreen → ResultadosScreen → InstalacionUsuarioScreen → ConfirmarReservaScreen |
| Mis reservas e historial | MisReservasScreen y DetalleReservaScreen |
| Eventos y equipos | EventosScreen, DetalleEventoScreen, CrearEquipoScreen |
| Calendario | CalendarioScreen, con rango permitido según perfil |
| Perfil | PerfilScreen, foto y acceso a preferencias |
| Preferencias y notificaciones | PreferenciasScreen y NotificacionesScreen para ambos roles |
| Inicio del administrador | AdminScreen y SolicitudesScreen |
| Instalaciones | InstalacionesScreen, FichaCanchaScreen y EditarCanchaScreen |
| Gestión de eventos | CrearEventoScreen y DetalleEventoScreen |
| Políticas y usuarios | PoliticasScreen, UsuariosScreen y DetalleUsuarioScreen |
| Reportes | ReportesScreen y ReportesModel, períodos, gráficos y CSV filtrado |
| Reprogramación | ReprogramarReservaScreen |
| Ayuda | AyudaScreen |
| Estructura común | AppScaffold, títulos por ruta, barra inferior o navegación lateral |

Cada pantalla principal tiene su archivo Kotlin. Se comparten las tarjetas, campos y selectores; las reglas y escrituras no se realizan desde los composables.

## Decisiones actuales

- Reservas de una hora, sin cantidad de participantes ni motivo.
- Aprobación o rechazo individual. Rechazar no requiere explicación escrita.
- Cancelación de reservas futuras confirmadas por titular o administración.
- Horarios configurables por días y por bloques de una hora, con jornada predeterminada 08:00–18:00.
- Las seis canchas de tenis se reservan de forma independiente.
- Notificaciones internas tipificadas y preferencias por cuenta. No se programan notificaciones push ni recordatorios del sistema.
- Reportes basados en registros locales; no se inventa un porcentaje de utilización de la infraestructura.
- Archivo lógico de usuarios e instalaciones para conservar referencias e historial.
- Recursos nativos de foto, archivo CSV y calendario con revisión del usuario.

El prototipo no es una implementación de autenticación o un servicio compartido. El código continúa funcionando con una base local por dispositivo y las cuentas de demostración del README.
