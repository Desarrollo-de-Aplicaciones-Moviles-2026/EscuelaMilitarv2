package com.example.aplicacinejercito.model

data class DatosApp(
    val usuarios: List<UsuarioModel> = emptyList(),
    val canchas: List<CanchaModel> = emptyList(),
    val reservas: List<ReservaModel> = emptyList(),
    val politicas: List<PoliticaModel> = emptyList(),
    val eventos: List<EventoModel> = emptyList(),
    val eventoCanchas: List<EventoCanchaModel> = emptyList(),
    val equipos: List<EquipoModel> = emptyList(),
    val integrantes: List<IntegranteModel> = emptyList(),
    val esperas: List<EsperaModel> = emptyList(),
    val notificaciones: List<NotificacionModel> = emptyList()
)

data class PreferenciasModel(
    val temaOscuro: Boolean = false,
    val notificacionesPredeterminadas: Boolean = true,
    val notificacionesPorUsuario: Map<String, Boolean> = emptyMap(),
    val tiposSilenciados: Map<String, Set<TipoNotificacion>> = emptyMap(),
    val fotos: Map<String, String> = emptyMap()
) {
    fun generales(usuarioId: String): Boolean = notificacionesPorUsuario[usuarioId] ?: notificacionesPredeterminadas
    fun tipoActivo(usuarioId: String, tipo: TipoNotificacion): Boolean = tipo !in tiposSilenciados[usuarioId].orEmpty()
    fun permite(usuarioId: String, tipo: TipoNotificacion): Boolean = generales(usuarioId) && tipoActivo(usuarioId, tipo)
}

data class AppUiState(
    val cargando: Boolean = true,
    val usuario: UsuarioModel? = null,
    val datos: DatosApp = DatosApp(),
    val preferencias: PreferenciasModel = PreferenciasModel(),
    val error: String? = null,
    val ahora: Long = 0L
)

data class LoginErrores(val correo: String? = null, val clave: String? = null)
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores(),
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val usuarioIngresado: UsuarioModel? = null
)

data class AccionUiState(val cargando: Boolean = false, val mensaje: String? = null, val error: String? = null)

data class ReservaErrores(
    val fecha: String? = null,
    val hora: String? = null,
    val normas: String? = null
)
data class ReservaFormulario(
    val canchaId: String = "",
    val tipo: String = "Todos",
    val fecha: String = "",
    val horaInicio: Int = 9,
    val aceptaNormas: Boolean = false,
    val errores: ReservaErrores = ReservaErrores()
)
data class EquipoErrores(val nombre: String? = null, val integrantes: String? = null)
data class EquipoFormulario(
    val eventoId: String = "", val nombre: String = "", val integrantes: Set<String> = emptySet(),
    val minimoIntegrantes: Int = 1, val errores: EquipoErrores = EquipoErrores()
)
data class CadeteUiState(
    val formulario: ReservaFormulario = ReservaFormulario(),
    val equipo: EquipoFormulario = EquipoFormulario(),
    val accion: AccionUiState = AccionUiState(),
    val reservaCreadaId: String? = null,
    val equipoCreado: Boolean = false
)

data class CanchaErrores(val nombre: String? = null, val capacidad: String? = null, val horario: String? = null)
data class CanchaFormulario(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "Multicancha techada",
    val capacidad: String = "30",
    val estado: EstadoCancha = EstadoCancha.HABILITADA,
    val apertura: String = "8",
    val cierre: String = "18",
    val dias: Set<Int> = (1..7).toSet(),
    val bloques: Set<Int> = (8..17).toSet(),
    val observaciones: String = "",
    val errores: CanchaErrores = CanchaErrores()
)
data class EventoErrores(val nombre: String? = null, val fecha: String? = null, val horario: String? = null, val equipos: String? = null, val instalaciones: String? = null)
data class EventoFormulario(
    val nombre: String = "",
    val disciplina: String = "Fútbol",
    val fecha: String = "",
    val inicio: String = "9",
    val fin: String = "13",
    val cupos: String = "8",
    val minimo: String = "5",
    val descripcion: String = "",
    val canchas: Set<String> = emptySet(),
    val errores: EventoErrores = EventoErrores()
)
data class AdminUiState(
    val cancha: CanchaFormulario = CanchaFormulario(),
    val evento: EventoFormulario = EventoFormulario(),
    val accion: AccionUiState = AccionUiState(),
    val guardado: Boolean = false,
    val politicas: List<PoliticaModel> = emptyList(),
    val reprogramacion: ReprogramacionFormulario = ReprogramacionFormulario(),
    val reporte: ReporteFiltros = ReporteFiltros()
)

data class ReprogramacionErrores(val fecha: String? = null, val hora: String? = null)
data class ReprogramacionFormulario(val reservaId: String = "", val fecha: String = "", val hora: String = "9", val errores: ReprogramacionErrores = ReprogramacionErrores())
