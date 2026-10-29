# Gestión Deportiva para Escuela Militar

Proyecto Android corregido para revisar los contenidos de Desarrollo de Apps Móviles de segundo año de Ingeniería Informática en Duoc UC. Conserva Kotlin, Jetpack Compose, Material 3, los perfiles de usuario y administración y la propuesta visual del prototipo adjunto.

**Versión 1.2**: aplica los 17 cambios solicitados. Incluye reservas sin participantes ni motivos, horarios visuales configurables, notificaciones por tipo, navegación a la raíz de cada sección y reportes con períodos y gráficos. Consulta [el informe de cambios](documentacion/INFORME_CAMBIOS.md).

Si ya usaste la versión 1.1, ejecuta esta versión sobre la instalación existente con Android Studio para aplicar la migración de Room 1 a 2 y conservar tus registros. No necesitas borrar los datos para actualizar. 
## Abrir y ejecutar

1. Descomprime el ZIP y abre la carpeta **EscuelaMilitarCorregida** desde **Open** en Android Studio. Selecciona la carpeta que contiene `settings.gradle.kts`.
2. Configura **Gradle JDK 17** en Settings → Build, Execution, Deployment → Build Tools → Gradle. Usa el Gradle Wrapper del proyecto.
3. En SDK Manager instala **Android SDK Platform 35** y **Android SDK Build Tools 34.0.0**. Android Studio puede ofrecer instalarlos al sincronizar.
4. Sincroniza Gradle. La primera sincronización necesita internet para obtener las dependencias.
5. Ejecuta `app` en un emulador o teléfono con Android 7.0 API 24 o superior.

No se incluye `local.properties`: Android Studio lo genera con la ubicación del SDK de tu computador. No copies una ruta de otro equipo. Android Studio Ladybug o una versión posterior compatible con AGP 8.7 permite abrir esta configuración.

Desde una terminal con JDK 17 y el SDK configurados:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

En Windows:

```bat
gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Si tu sistema no conserva el permiso de ejecución del archivo `gradlew`, usa `bash gradlew` o `chmod +x gradlew`.

## Cuentas de demostración

La contraseña para las cuentas de prueba es **Clave123**. El inicio de sesión es local y comprueba una cuenta precargada; no autentica personas reales ante la institución.

| Perfil | Correo | Cupo inicial semanal | Anticipación inicial |
| --- | --- | --- | --- |
| Cadete Martín Leiva | m.leiva@escuelamilitar.cl | 1 token | 3 días |
| Cabo Diego Morales | d.morales@escuelamilitar.cl | 2 tokens | 7 días |
| Teniente Valentina Soto | v.soto@escuelamilitar.cl | 3 tokens | 14 días |
| Administración | admin@escuelamilitar.cl | Administra las políticas | No realiza reservas personales |

También están disponibles c.perez@escuelamilitar.cl, n.vidal@escuelamilitar.cl, t.riquelme@escuelamilitar.cl y a.rojas@escuelamilitar.cl. La cuenta i.fuentes@escuelamilitar.cl está archivada para probar el bloqueo de acceso. Los demás integrantes de compañías son datos ficticios de demostración.

La primera apertura crea 20 espacios (incluidas seis canchas de tenis independientes), las compañías, cinco competencias y dos reservas de muestra coherentes con la fecha de instalación. Las siguientes aperturas conservan los datos de Room. Para comenzar otra demostración desde cero, borra los datos de la app desde los ajustes de Android o desinstálala y vuelve a instalarla. Cerrar sesión conserva la información.

## Cómo probar un flujo completo

1. Entra como **m.leiva@escuelamilitar.cl** y crea una reserva para mañana, por ejemplo en tenis a las 11:00. Acepta las normas y confirma.
2. Revisa que la solicitud aparezca **Pendiente** en Mis reservas y que quede ocupado su token semanal.
3. Ve a Perfil → Cerrar sesión. Entra como **admin@escuelamilitar.cl**, abre Solicitudes y aprueba o rechaza la solicitud desde su detalle.
4. Vuelve a entrar como Martín. Comprueba el cambio de estado y la notificación correspondiente.
5. En una reserva futura confirmada, usa Cancelar. El bloque quedará libre y el token de la semana de solicitud dejará de contar.

Los resultados reales de compilación y las 35 pruebas automatizadas están en [VERIFICACION.md](documentacion/VERIFICACION.md).

La [guía de pruebas](documentacion/PRUEBAS.md) incluye más casos para el teléfono o emulador. [El mapa del prototipo](documentacion/MAPA_PROTOTIPO.md) indica dónde quedó cada grupo de pantallas del HTML.

## Organización

| Carpeta | Responsabilidad |
| --- | --- |
| `model/` | Entidades, estados de formularios, errores y reglas comprobables |
| `viewmodel/` | Estado observable con StateFlow y respuesta a eventos de la interfaz |
| `repository/` | Operaciones de datos, permisos por rol y transacciones |
| `data/` | Room, DAO y datos iniciales de demostración |
| `ui/screens/` | Una pantalla Compose por archivo Kotlin |
| `ui/components/` | Campos, botones, tarjetas y Scaffold compartidos |
| `ui/theme/` | Colores, tipografía, formas y temas claro y oscuro |
| `navigation/` | Rutas y NavHost |

La app usa inyección manual con `AppViewModelFactory`. No requiere Hilt, Firebase, un servidor, una cuenta externa ni claves de API.

## Alcance

Los datos viven **en un solo dispositivo**. Las cuentas, roles y listas de espera son una demostración académica. Para un servicio compartido entre teléfonos se necesita un backend que autentique y autorice las operaciones y resuelva la concurrencia entre dispositivos.

Las preferencias de tema, notificaciones y foto se guardan con DataStore. Las preferencias de notificaciones son independientes para cada cuenta. Las reservas, usuarios, instalaciones, eventos y equipos se guardan con Room. La sesión y los borradores de formularios viven en los ViewModels: sobreviven a una rotación, pero se pide ingresar nuevamente si Android termina el proceso.

Las notificaciones son internas. El calendario abre la aplicación de calendario del dispositivo para que la persona revise y guarde el evento. No se programan recordatorios ni notificaciones push. El selector de imagen usa una API nativa sin pedir acceso general a los archivos.

Esta entrega está alineada con el material adjunto. No se adjuntó una pauta de evaluación que permita certificar todos los requisitos de una evaluación o afirmar una nota.
