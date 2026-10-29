package com.example.aplicacinejercito.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.navigation.Rutas

private data class Destino(val ruta: String, val titulo: String, val icono: ImageVector)

internal fun seccionDe(ruta: String, admin: Boolean): String = when (ruta) {
    Rutas.NUEVA_RESERVA, Rutas.RESULTADOS, Rutas.INSTALACION_USUARIO, Rutas.CONFIRMAR -> Rutas.RESERVAS
    Rutas.DETALLE_RESERVA, Rutas.REPROGRAMAR, Rutas.SOLICITUDES -> if (admin) Rutas.CALENDARIO else Rutas.RESERVAS
    Rutas.DETALLE_EVENTO, Rutas.EQUIPO, Rutas.NUEVO_EVENTO -> Rutas.EVENTOS
    Rutas.FICHA, Rutas.EDITAR_CANCHA -> Rutas.INSTALACIONES
    Rutas.HISTORIAL, Rutas.AYUDA -> if (admin) Rutas.CONFIGURACION else Rutas.PERFIL
    Rutas.POLITICAS, Rutas.USUARIOS, Rutas.DETALLE_USUARIO, Rutas.REPORTES, Rutas.INSTITUCIONAL, Rutas.PREFERENCIAS -> if (admin) Rutas.CONFIGURACION else Rutas.PERFIL
    Rutas.PERFIL -> if (admin) Rutas.CONFIGURACION else Rutas.PERFIL
    else -> ruta
}

private fun tituloPantalla(ruta: String, admin: Boolean) = when (ruta) {
    Rutas.INICIO -> if (admin) "Panel de administración" else "Inicio"
    Rutas.RESERVAS -> "Mis reservas"
    Rutas.EVENTOS -> "Eventos y competencias"
    Rutas.CALENDARIO -> "Calendario de instalaciones"
    Rutas.PERFIL -> "Mi perfil"
    Rutas.NUEVA_RESERVA -> "Buscar una instalación"
    Rutas.RESULTADOS -> "Instalaciones encontradas"
    Rutas.INSTALACION_USUARIO -> "Detalle de la instalación"
    Rutas.CONFIRMAR -> "Solicitar reserva"
    Rutas.DETALLE_RESERVA -> "Detalle de la reserva"
    Rutas.HISTORIAL -> "Historial de reservas"
    Rutas.DETALLE_EVENTO -> "Detalle del evento"
    Rutas.EQUIPO -> "Inscribir un equipo"
    Rutas.NOTIFICACIONES -> "Notificaciones"
    Rutas.AYUDA -> "Ayuda y normas de uso"
    Rutas.SOLICITUDES -> "Solicitudes pendientes"
    Rutas.INSTALACIONES -> "Instalaciones"
    Rutas.FICHA -> "Ficha de la instalación"
    Rutas.EDITAR_CANCHA -> "Datos y horarios"
    Rutas.NUEVO_EVENTO -> "Crear un evento"
    Rutas.CONFIGURACION -> "Configuración"
    Rutas.POLITICAS -> "Políticas de reserva"
    Rutas.USUARIOS -> "Usuarios"
    Rutas.DETALLE_USUARIO -> "Ficha del usuario"
    Rutas.REPORTES -> "Reportes de reservas"
    Rutas.INSTITUCIONAL -> "Calendario institucional"
    Rutas.REPROGRAMAR -> "Reprogramar reserva"
    Rutas.PREFERENCIAS -> "Preferencias y notificaciones"
    else -> "Inicio"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(nav: NavHostController, estado: AppUiState, usarRail: Boolean, cargando: Boolean,
                snackbar: SnackbarHostState, contenido: @Composable () -> Unit) {
    val usuario = estado.usuario ?: return
    BackHandler(enabled = cargando) { }
    val admin = usuario.rol == Rol.ADMIN
    val destinos = if (admin) listOf(
        Destino(Rutas.INICIO, "Inicio", Icons.Default.Home), Destino(Rutas.CALENDARIO, "Agenda", Icons.Default.CalendarMonth),
        Destino(Rutas.EVENTOS, "Eventos", Icons.Default.EmojiEvents), Destino(Rutas.INSTALACIONES, "Espacios", Icons.Default.Stadium),
        Destino(Rutas.CONFIGURACION, "Ajustes", Icons.Default.Settings)
    ) else listOf(
        Destino(Rutas.INICIO, "Inicio", Icons.Default.Home), Destino(Rutas.RESERVAS, "Reservas", Icons.Default.EventAvailable),
        Destino(Rutas.EVENTOS, "Eventos", Icons.Default.EmojiEvents), Destino(Rutas.CALENDARIO, "Agenda", Icons.Default.CalendarMonth),
        Destino(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )
    val entrada by nav.currentBackStackEntryAsState()
    val actual = entrada?.destination?.route ?: Rutas.INICIO
    val seccion = seccionDe(actual, admin)
    fun ir(destino: String) {
        nav.navigate(destino) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
        }
    }
    val nombre = usuario.nombre.substringBefore(' ')
    val hayNotificaciones = estado.datos.notificaciones.any { it.usuarioId == usuario.id && estado.preferencias.permite(usuario.id, it.tipo) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(title = {
                    Column {
                        if (actual == Rutas.INICIO) Text(if (admin) Fechas.etiqueta(Fechas.hoy().toString()) else "${usuario.grado.etiqueta} · ${Fechas.etiqueta(Fechas.hoy().toString())}", style = MaterialTheme.typography.labelMedium)
                        Text(if (actual == Rutas.INICIO && !admin) "Hola, $nombre" else tituloPantalla(actual, admin),
                            style = if (actual == Rutas.INICIO && !admin) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }, navigationIcon = {
                    if (destinos.none { it.ruta == actual }) IconButton(onClick = { nav.popBackStack() }, enabled = !cargando) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }, actions = {
                    IconButton(onClick = { nav.navigate(Rutas.NOTIFICACIONES) { launchSingleTop = true } }, enabled = !cargando) {
                        BadgedBox(badge = { if (hayNotificaciones) Badge(containerColor = MaterialTheme.colorScheme.secondary) }) { Icon(Icons.Default.NotificationsNone, "Notificaciones") }
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary))
                if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }, bottomBar = {
            if (!usarRail) NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                destinos.forEach { d -> NavigationBarItem(selected = seccion == d.ruta, onClick = { ir(d.ruta) }, enabled = !cargando,
                    icon = { Icon(d.icono, d.titulo) }, label = { Text(d.titulo, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer, selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant)) }
            }
        }, snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (usarRail) NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                destinos.forEach { d -> NavigationRailItem(selected = seccion == d.ruta, onClick = { ir(d.ruta) }, enabled = !cargando,
                    icon = { Icon(d.icono, null) }, label = { Text(d.titulo, maxLines = 1) }) }
            }
            Box(Modifier.weight(1f).fillMaxHeight().imePadding()) { contenido() }
        }
    }
}
