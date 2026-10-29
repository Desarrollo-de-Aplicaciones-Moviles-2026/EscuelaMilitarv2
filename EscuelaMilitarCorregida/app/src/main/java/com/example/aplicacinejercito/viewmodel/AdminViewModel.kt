package com.example.aplicacinejercito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.repository.DeportesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: DeportesRepository) : ViewModel() {
    private val _estado = MutableStateFlow(AdminUiState())
    val estado = _estado.asStateFlow()
    fun limpiar() { _estado.value = AdminUiState() }
    fun prepararCancha(c: CanchaModel?) {
        _estado.update { it.copy(cancha = c?.let { CanchaFormulario(it.id, it.nombre, it.tipo, it.capacidad.toString(), it.estado, it.horaApertura.toString(), it.horaCierre.toString(), Horarios.dias(it.diasActivos), Horarios.horas(it.bloquesDisponibles), it.observaciones) } ?: CanchaFormulario(), guardado = false, accion = AccionUiState()) }
    }
    fun actualizarCancha(cambio: (CanchaFormulario) -> CanchaFormulario) {
        if (!_estado.value.accion.cargando) _estado.update { it.copy(cancha = cambio(it.cancha).copy(errores = CanchaErrores()), accion = AccionUiState()) }
    }
    fun guardarCancha(adminId: String) {
        val f = _estado.value.cancha
        val errores = ValidadorCancha.validar(f)
        _estado.update { it.copy(cancha = f.copy(errores = errores)) }
        if (errores == CanchaErrores()) ejecutar { repository.guardarCancha(adminId, f); _estado.update { it.copy(guardado = true) }; "Instalación guardada." }
    }
    fun prepararEvento() { _estado.update { it.copy(evento = EventoFormulario(fecha = Fechas.hoy().plusDays(1).toString()), guardado = false, accion = AccionUiState()) } }
    fun actualizarEvento(cambio: (EventoFormulario) -> EventoFormulario) {
        if (!_estado.value.accion.cargando) _estado.update { it.copy(evento = cambio(it.evento).copy(errores = EventoErrores()), accion = AccionUiState()) }
    }
    fun guardarEvento(adminId: String) {
        val f = _estado.value.evento
        val errores = ValidadorEvento.validar(f, Fechas.hoy())
        _estado.update { it.copy(evento = f.copy(errores = errores)) }
        if (errores == EventoErrores()) ejecutar { repository.crearEvento(adminId, f); _estado.update { it.copy(guardado = true) }; "Evento creado." }
    }
    fun consumirGuardado() { _estado.update { it.copy(guardado = false) } }
    fun prepararPoliticas(politicas: List<PoliticaModel>) { _estado.update { it.copy(politicas = politicas, accion = AccionUiState()) } }
    fun cambiarPolitica(grado: Grado, tokens: Int = 0, dias: Int = 0, aprobacion: Boolean? = null) {
        if (_estado.value.accion.cargando) return
        _estado.update { estado -> estado.copy(politicas = estado.politicas.map {
            if (it.grado == grado) it.copy(tokensSemana = (it.tokensSemana + tokens).coerceIn(1, 20),
                anticipacionDias = (it.anticipacionDias + dias).coerceIn(1, 30), requiereAprobacion = aprobacion ?: it.requiereAprobacion) else it
        }) }
    }
    fun prepararReprogramacion(reserva: ReservaModel) {
        _estado.update { it.copy(reprogramacion = ReprogramacionFormulario(reserva.id, reserva.fecha, reserva.horaInicio.toString()), guardado = false, accion = AccionUiState()) }
    }
    fun cambiarReprogramacion(cambio: (ReprogramacionFormulario) -> ReprogramacionFormulario) {
        if (!_estado.value.accion.cargando) _estado.update { it.copy(reprogramacion = cambio(it.reprogramacion).copy(errores = ReprogramacionErrores()), accion = AccionUiState()) }
    }
    fun guardarReprogramacion(adminId: String) {
        val f = _estado.value.reprogramacion
        val fecha = Fechas.leer(f.fecha)
        val hora = f.hora.toIntOrNull()
        val errores = ReprogramacionErrores(
            if (fecha == null || fecha.isBefore(Fechas.hoy())) "Ingresa una fecha válida desde hoy, en formato AAAA-MM-DD." else null,
            if (hora == null || hora !in 0..23) "Ingresa una hora entera de 0 a 23." else null
        )
        _estado.update { it.copy(reprogramacion = f.copy(errores = errores)) }
        if (errores != ReprogramacionErrores()) return
        ejecutar {
            repository.reprogramarReserva(adminId, f.reservaId, f.fecha, hora!!)
            _estado.update { it.copy(guardado = true) }
            "Reserva reprogramada."
        }
    }
    fun cambiarReporte(cambio: (ReporteFiltros) -> ReporteFiltros) { _estado.update { it.copy(reporte = cambio(it.reporte)) } }
    fun archivarCancha(adminId: String, id: String, archivar: Boolean) = ejecutar { repository.archivarCancha(adminId, id, archivar); if (archivar) "Instalación archivada." else "Instalación restaurada." }
    fun resolver(adminId: String, reservaId: String, aprobar: Boolean) = ejecutar {
        repository.resolverSolicitud(adminId, reservaId, aprobar)
        "Solicitud procesada."
    }
    fun cancelarReserva(adminId: String, reservaId: String) = ejecutar { repository.cancelarReserva(adminId, reservaId); "Reserva cancelada." }
    fun guardarPoliticas(adminId: String, politicas: List<PoliticaModel>) = ejecutar { repository.guardarPoliticas(adminId, politicas); "Políticas guardadas." }
    fun cambiarEstadoUsuario(adminId: String, id: String, activo: Boolean) = ejecutar { repository.cambiarEstadoUsuario(adminId, id, activo); "Estado de la cuenta actualizado." }
    fun cancelarEvento(adminId: String, id: String) = ejecutar { repository.cancelarEvento(adminId, id); "Evento cancelado." }
    fun retirarEquipo(adminId: String, equipo: EquipoModel) = ejecutar { repository.retirarEquipo(adminId, equipo); "Equipo retirado." }
    fun limpiarMensaje() { _estado.update { it.copy(accion = it.accion.copy(mensaje = null, error = null)) } }
    private fun ejecutar(operacion: suspend () -> String) {
        if (_estado.value.accion.cargando) return
        _estado.update { it.copy(accion = AccionUiState(cargando = true)) }
        viewModelScope.launch {
            try { val mensaje = operacion(); _estado.update { it.copy(accion = AccionUiState(mensaje = mensaje)) } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _estado.update { it.copy(accion = AccionUiState(error = mensajeError(e))) } }
        }
    }
}
