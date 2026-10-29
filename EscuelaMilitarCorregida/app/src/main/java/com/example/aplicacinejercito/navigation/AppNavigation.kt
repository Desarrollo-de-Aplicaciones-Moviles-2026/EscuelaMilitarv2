package com.example.aplicacinejercito.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*
import com.example.aplicacinejercito.ui.screens.*
import com.example.aplicacinejercito.viewmodel.*

@Composable
fun AppNavigation(estado: AppUiState, ancho: WindowWidthSizeClass, factory: AppViewModelFactory, app: AppViewModel) {
    val login: LoginViewModel = viewModel(factory = factory)
    val cadete: CadeteViewModel = viewModel(factory = factory)
    val admin: AdminViewModel = viewModel(factory = factory)
    val acceso by login.estado.collectAsStateWithLifecycle()
    val usuarioUi by cadete.estado.collectAsStateWithLifecycle()
    val adminUi by admin.estado.collectAsStateWithLifecycle()
    LaunchedEffect(acceso.usuarioIngresado) {
        acceso.usuarioIngresado?.let { app.ingresar(it); login.limpiar() }
    }
    if (estado.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (estado.datos.usuarios.isEmpty()) {
        Pagina { Text(estado.error ?: "No fue posible abrir los datos."); BotonPersonalizado("Reintentar", app::cargar) }
        return
    }
    val usuario = estado.usuario
    if (usuario == null) {
        LoginScreen(acceso, login::cambiarCorreo, login::cambiarClave, login::ingresar, ancho == WindowWidthSizeClass.Expanded)
        return
    }
    // Al cambiar de cuenta se crea otro NavHost. Atrás nunca permite volver a una sesión cerrada.
    key(usuario.id) {
        val nav = rememberNavController()
        val snackbar = remember { SnackbarHostState() }
        val esAdmin = usuario.rol == Rol.ADMIN
        val accion = if (esAdmin) adminUi.accion else usuarioUi.accion
        val politica = estado.datos.politicas.firstOrNull { it.grado == usuario.grado } ?: PoliticaModel(usuario.grado, 1, 3)
        LaunchedEffect(accion.mensaje, accion.error) {
            (accion.error ?: accion.mensaje)?.let { snackbar.showSnackbar(it) }
            if (esAdmin) admin.limpiarMensaje() else cadete.limpiarMensaje()
        }
        LaunchedEffect(estado.error) {
            estado.error?.let { snackbar.showSnackbar(it); app.limpiarError() }
        }
        LaunchedEffect(usuarioUi.reservaCreadaId) {
            usuarioUi.reservaCreadaId?.let { id ->
                if (!esAdmin) nav.navigate(Rutas.detalleReserva(id)) { popUpTo(Rutas.INICIO) }
                cadete.consumirCreacion()
            }
        }
        LaunchedEffect(usuarioUi.equipoCreado) {
            if (usuarioUi.equipoCreado) { nav.popBackStack(); cadete.consumirCreacion() }
        }
        LaunchedEffect(adminUi.guardado) {
            if (adminUi.guardado) { nav.popBackStack(); admin.consumirGuardado() }
        }
        val verReserva: (String) -> Unit = { nav.navigate(Rutas.detalleReserva(it)) }
        AppScaffold(nav, estado, ancho != WindowWidthSizeClass.Compact, accion.cargando, snackbar) {
            NavHost(navController = nav, startDestination = Rutas.INICIO) {
                composable(Rutas.INICIO) {
                    if (esAdmin) AdminScreen(estado, { nav.navigate(Rutas.SOLICITUDES) }, { nav.navigate(Rutas.INSTALACIONES) }, verReserva)
                    else CadeteScreen(estado,
                        { cadete.nuevaReserva(); nav.navigate(Rutas.NUEVA_RESERVA) }, verReserva,
                        { nav.navigate(Rutas.EVENTOS) }, { nav.navigate(Rutas.detalleEvento(it)) }, { nav.navigate(Rutas.RESERVAS) })
                }
                composable(Rutas.CALENDARIO) { CalendarioScreen(estado.datos, verReserva, anticipacion = if (esAdmin) null else politica.anticipacionDias) }
                composable(Rutas.NOTIFICACIONES) { NotificacionesScreen(estado, { nav.navigate(Rutas.PREFERENCIAS) }) }
                composable(Rutas.AYUDA) { AyudaScreen(estado.datos.politicas) }
                composable(Rutas.PREFERENCIAS) { PreferenciasScreen(estado, app::cambiarTema, app::cambiarNotificaciones, app::cambiarTipoNotificacion) }
                composable(Rutas.PERFIL) {
                    PerfilScreen(estado, { nav.navigate(Rutas.PREFERENCIAS) }, app::guardarFoto,
                        { nav.navigate(Rutas.HISTORIAL) }, { nav.navigate(Rutas.AYUDA) }, {
                            if (!accion.cargando) { cadete.nuevaReserva(); admin.limpiar(); login.limpiar(); app.cerrarSesion() }
                        }, ocupada = accion.cargando)
                }
                composable(Rutas.DETALLE_RESERVA) { entrada ->
                    val reserva = estado.datos.reservas.firstOrNull { it.id == entrada.arguments?.getString("id") }
                    if (reserva != null && !esAdmin && reserva.usuarioId != usuario.id) {
                        Pagina {
                            TituloSeccion("Bloque ocupado")
                            Text(estado.datos.canchas.firstOrNull { it.id == reserva.canchaId }?.nombre.orEmpty())
                            Text("${Fechas.etiqueta(reserva.fecha)} · ${Fechas.horario(reserva.horaInicio)}")
                        }
                    } else DetalleReservaScreen(reserva, estado.datos, esAdmin, accion.cargando,
                        cancelar = { reserva?.let { if (esAdmin) admin.cancelarReserva(usuario.id, it.id) else cadete.cancelar(usuario.id, it.id) } },
                        aprobar = { reserva?.let { admin.resolver(usuario.id, it.id, true) } },
                        rechazar = { reserva?.let { admin.resolver(usuario.id, it.id, false) } },
                        modificar = { reserva?.let { admin.prepararReprogramacion(it); nav.navigate(Rutas.REPROGRAMAR) } })
                }
                composable(Rutas.EVENTOS) {
                    EventosScreen(estado.datos, usuario, { nav.navigate(Rutas.detalleEvento(it)) }, { admin.prepararEvento(); nav.navigate(Rutas.NUEVO_EVENTO) })
                }
                composable(Rutas.DETALLE_EVENTO) { entrada ->
                    val evento = estado.datos.eventos.firstOrNull { it.id == entrada.arguments?.getString("id") }
                    DetalleEventoScreen(evento, estado.datos, usuario, accion.cargando,
                        crearEquipo = { evento?.let { cadete.prepararEquipo(it, usuario.id); nav.navigate(Rutas.EQUIPO) } },
                        retirar = { if (esAdmin) admin.retirarEquipo(usuario.id, it) else cadete.retirarEquipo(usuario.id, it) },
                        cancelar = { evento?.let { admin.cancelarEvento(usuario.id, it.id) } })
                }
                if (!esAdmin) {
                    composable(Rutas.RESERVAS) { MisReservasScreen(estado.datos, usuario.id, verReserva) }
                    composable(Rutas.HISTORIAL) { MisReservasScreen(estado.datos, usuario.id, verReserva, historial = true) }
                    composable(Rutas.NUEVA_RESERVA) {
                        NuevaReservaScreen(usuarioUi.formulario, estado.datos, politica, cadete::actualizarFormulario, {
                            if (cadete.validarBusqueda(politica.anticipacionDias)) nav.navigate(Rutas.RESULTADOS)
                        })
                    }
                    composable(Rutas.RESULTADOS) {
                        ResultadosScreen(usuarioUi.formulario, estado.datos, { id -> cadete.seleccionarCancha(id); nav.navigate(Rutas.instalacionUsuario(id)) }, { nav.popBackStack() })
                    }
                    composable(Rutas.INSTALACION_USUARIO) { entrada ->
                        val cancha = estado.datos.canchas.firstOrNull { it.id == entrada.arguments?.getString("id") }
                        InstalacionUsuarioScreen(cancha, usuarioUi.formulario, estado.datos, usuario.id,
                            { nav.navigate(Rutas.CONFIRMAR) }, { cadete.alternarEspera(usuario.id) }, accion.cargando)
                    }
                    composable(Rutas.CONFIRMAR) {
                        ConfirmarReservaScreen(usuarioUi.formulario, estado.datos.canchas.firstOrNull { it.id == usuarioUi.formulario.canchaId }, politica, (politica.tokensSemana - ReglasReserva.tokensUsados(estado.datos.reservas, usuario.id, Fechas.hoy())).coerceAtLeast(0),
                            accion, cadete::actualizarFormulario, { cadete.solicitar(usuario.id, politica.anticipacionDias) })
                    }
                    composable(Rutas.EQUIPO) {
                        CrearEquipoScreen(usuarioUi.equipo, estado.datos.eventos.firstOrNull { it.id == usuarioUi.equipo.eventoId }, estado.datos, usuario,
                            accion, cadete::actualizarEquipo, { cadete.crearEquipo(usuario.id) })
                    }
                } else {
                    composable(Rutas.SOLICITUDES) { SolicitudesScreen(estado.datos, verReserva) }
                    composable(Rutas.INSTALACIONES) {
                        InstalacionesScreen(estado.datos, { admin.prepararCancha(null); nav.navigate(Rutas.EDITAR_CANCHA) }, { nav.navigate(Rutas.ficha(it)) })
                    }
                    composable(Rutas.FICHA) { entrada ->
                        val cancha = estado.datos.canchas.firstOrNull { it.id == entrada.arguments?.getString("id") }
                        FichaCanchaScreen(cancha, estado.datos, { admin.prepararCancha(cancha); nav.navigate(Rutas.EDITAR_CANCHA) },
                            { archivar -> cancha?.let { admin.archivarCancha(usuario.id, it.id, archivar) } }, verReserva, accion.cargando)
                    }
                    composable(Rutas.EDITAR_CANCHA) { EditarCanchaScreen(adminUi.cancha, accion, admin::actualizarCancha, { admin.guardarCancha(usuario.id) }) }
                    composable(Rutas.NUEVO_EVENTO) { CrearEventoScreen(adminUi.evento, estado.datos, accion, admin::actualizarEvento, { admin.guardarEvento(usuario.id) }) }
                    composable(Rutas.REPROGRAMAR) {
                        val titularId = estado.datos.reservas.firstOrNull { it.id == adminUi.reprogramacion.reservaId }?.usuarioId
                        val grado = estado.datos.usuarios.firstOrNull { it.id == titularId }?.grado
                        val dias = estado.datos.politicas.firstOrNull { it.grado == grado }?.anticipacionDias ?: 3
                        ReprogramarReservaScreen(adminUi.reprogramacion, dias, accion, admin::cambiarReprogramacion, { admin.guardarReprogramacion(usuario.id) })
                    }
                    composable(Rutas.CONFIGURACION) {
                        ConfiguracionScreen({ nav.navigate(Rutas.PERFIL) }, { admin.prepararPoliticas(estado.datos.politicas); nav.navigate(Rutas.POLITICAS) },
                            { nav.navigate(Rutas.USUARIOS) }, { nav.navigate(Rutas.REPORTES) }, { nav.navigate(Rutas.INSTITUCIONAL) }, { nav.navigate(Rutas.NOTIFICACIONES) }, { nav.navigate(Rutas.PREFERENCIAS) })
                    }
                    composable(Rutas.POLITICAS) { PoliticasScreen(adminUi.politicas, accion, admin::cambiarPolitica, { admin.guardarPoliticas(usuario.id, adminUi.politicas) }) }
                    composable(Rutas.USUARIOS) { UsuariosScreen(estado.datos, { nav.navigate(Rutas.detalleUsuario(it)) }) }
                    composable(Rutas.DETALLE_USUARIO) { entrada ->
                        val persona = estado.datos.usuarios.firstOrNull { it.id == entrada.arguments?.getString("id") }
                        DetalleUsuarioScreen(persona, estado.datos, accion.cargando,
                            { activo -> persona?.let { admin.cambiarEstadoUsuario(usuario.id, it.id, activo) } }, verReserva)
                    }
                    composable(Rutas.REPORTES) { ReportesScreen(estado.datos, adminUi.reporte, admin::cambiarReporte) }
                    composable(Rutas.INSTITUCIONAL) { CalendarioScreen(estado.datos, verReserva, institucional = true) }
                }
            }
        }
    }
}
