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

class CadeteViewModel(private val repository: DeportesRepository) : ViewModel() {
    private fun inicial() = CadeteUiState(formulario = ReservaFormulario(fecha = Fechas.hoy().plusDays(1).toString()))
    private val _estado = MutableStateFlow(inicial())
    val estado = _estado.asStateFlow()
    fun nuevaReserva() { _estado.value = inicial() }
    fun actualizarFormulario(cambio: (ReservaFormulario) -> ReservaFormulario) {
        if (_estado.value.accion.cargando) return
        _estado.update { it.copy(formulario = cambio(it.formulario).copy(errores = ReservaErrores()), accion = AccionUiState()) }
    }
    fun seleccionarCancha(id: String) = actualizarFormulario { it.copy(canchaId = id) }
    fun validarBusqueda(anticipacion: Int): Boolean {
        val f = _estado.value.formulario
        val errores = ReglasReserva.validarFormulario(f, Fechas.hoy(), anticipacion, exigirNormas = false)
        _estado.update { it.copy(formulario = f.copy(errores = errores)) }
        return errores == ReservaErrores()
    }
    fun solicitar(usuarioId: String, anticipacion: Int) {
        if (_estado.value.accion.cargando) return
        val f = _estado.value.formulario
        val errores = ReglasReserva.validarFormulario(f, Fechas.hoy(), anticipacion)
        _estado.update { it.copy(formulario = f.copy(errores = errores)) }
        if (errores != ReservaErrores()) return
        ejecutar {
            val id = repository.solicitarReserva(usuarioId, f)
            _estado.update { it.copy(reservaCreadaId = id) }
            "Solicitud registrada."
        }
    }
    fun consumirCreacion() { _estado.update { it.copy(reservaCreadaId = null, equipoCreado = false) } }
    fun cancelar(usuarioId: String, id: String) = ejecutar { repository.cancelarReserva(usuarioId, id); "Reserva cancelada." }
    fun alternarEspera(usuarioId: String) = ejecutar { repository.alternarEspera(usuarioId, _estado.value.formulario) }
    fun prepararEquipo(evento: EventoModel, usuarioId: String) {
        _estado.update { it.copy(equipo = EquipoFormulario(evento.id, integrantes = setOf(usuarioId), minimoIntegrantes = evento.minimoIntegrantes), accion = AccionUiState(), equipoCreado = false) }
    }
    fun actualizarEquipo(cambio: (EquipoFormulario) -> EquipoFormulario) {
        if (!_estado.value.accion.cargando) _estado.update { it.copy(equipo = cambio(it.equipo).copy(errores = EquipoErrores()), accion = AccionUiState()) }
    }
    fun crearEquipo(usuarioId: String) {
        val formulario = _estado.value.equipo
        val errores = ValidadorEquipo.validar(formulario)
        _estado.update { it.copy(equipo = formulario.copy(errores = errores)) }
        if (errores != EquipoErrores()) return
        ejecutar {
            repository.crearEquipo(usuarioId, formulario)
            _estado.update { it.copy(equipoCreado = true) }
            "Equipo inscrito."
        }
    }
    fun retirarEquipo(usuarioId: String, equipo: EquipoModel) = ejecutar { repository.retirarEquipo(usuarioId, equipo); "Equipo retirado." }
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
