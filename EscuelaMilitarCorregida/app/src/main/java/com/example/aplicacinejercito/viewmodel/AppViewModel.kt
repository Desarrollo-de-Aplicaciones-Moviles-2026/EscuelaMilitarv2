package com.example.aplicacinejercito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.repository.DeportesRepository
import com.example.aplicacinejercito.repository.PreferenciasRepository
import com.example.aplicacinejercito.repository.ReglaNegocioException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal fun mensajeError(error: Exception): String = if (error is ReglaNegocioException)
    error.message ?: "Revisa los datos." else "No fue posible completar la operación. Vuelve a intentarlo."

/** Instancia compartida por las pantallas del NavHost, como se muestra en clases. */
class AppViewModel(private val repository: DeportesRepository, private val preferencias: PreferenciasRepository) : ViewModel() {
    private val usuarioId = MutableStateFlow<String?>(null)
    private val ahora = MutableStateFlow(System.currentTimeMillis())
    private val _estado = MutableStateFlow(AppUiState())
    val estado = _estado.asStateFlow()
    private var carga: Job? = null

    init {
        cargar()
        viewModelScope.launch { while (isActive) { delay(60_000); ahora.value = System.currentTimeMillis() } }
    }
    fun cargar() {
        carga?.cancel()
        _estado.update { it.copy(cargando = true, error = null) }
        carga = viewModelScope.launch {
            try {
                repository.inicializar()
                combine(repository.observarDatos(), usuarioId, preferencias.preferencias, ahora) { datos, id, prefs, tiempo ->
                    AppUiState(cargando = false, usuario = datos.usuarios.firstOrNull { it.id == id && it.activo }, datos = datos, preferencias = prefs, ahora = tiempo)
                }.collect { _estado.value = it }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _estado.update { it.copy(cargando = false, error = "No fue posible abrir los datos locales. Puedes reintentar.") } }
        }
    }
    fun ingresar(usuario: UsuarioModel) { usuarioId.value = usuario.id }
    fun cerrarSesion() { usuarioId.value = null }
    fun cambiarTema(oscuro: Boolean) = guardarPreferencia { preferencias.cambiarTema(oscuro) }
    fun cambiarNotificaciones(activos: Boolean) {
        val id = usuarioId.value ?: return
        guardarPreferencia { preferencias.cambiarNotificaciones(id, activos) }
    }
    fun cambiarTipoNotificacion(tipo: TipoNotificacion, activo: Boolean) {
        val id = usuarioId.value ?: return
        guardarPreferencia { preferencias.cambiarTipo(id, tipo, activo) }
    }
    fun guardarFoto(uri: String) {
        val id = usuarioId.value ?: return
        guardarPreferencia { preferencias.guardarFoto(id, uri) }
    }
    fun limpiarError() { _estado.update { it.copy(error = null) } }
    private fun guardarPreferencia(operacion: suspend () -> Unit) {
        viewModelScope.launch {
            try { operacion() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _estado.update { it.copy(error = "No se pudo guardar la preferencia.") } }
        }
    }
}

class AppViewModelFactory(private val repository: DeportesRepository, private val preferencias: PreferenciasRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(repository, preferencias)
        LoginViewModel::class.java -> LoginViewModel(repository)
        CadeteViewModel::class.java -> CadeteViewModel(repository)
        AdminViewModel::class.java -> AdminViewModel(repository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
