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

class LoginViewModel(private val repository: DeportesRepository) : ViewModel() {
    private val _estado = MutableStateFlow(LoginUiState())
    val estado = _estado.asStateFlow()
    fun cambiarCorreo(valor: String) = editar { it.copy(correo = valor) }
    fun cambiarClave(valor: String) = editar { it.copy(clave = valor) }
    private fun editar(cambio: (LoginUiState) -> LoginUiState) {
        if (_estado.value.cargando) return
        _estado.update {
            val nuevo = cambio(it).copy(error = null)
            if (nuevo.intentoEnvio) nuevo.copy(errores = ValidadorLogin.validar(nuevo.correo, nuevo.clave)) else nuevo
        }
    }
    fun ingresar() {
        val actual = _estado.value
        if (actual.cargando) return
        val errores = ValidadorLogin.validar(actual.correo, actual.clave)
        _estado.update { it.copy(errores = errores, intentoEnvio = true, error = null) }
        if (errores != LoginErrores()) return
        _estado.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                val usuario = repository.autenticar(actual.correo, actual.clave)
                _estado.value = LoginUiState(usuarioIngresado = usuario)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _estado.update { it.copy(cargando = false, error = mensajeError(e)) } }
        }
    }
    fun limpiar() { _estado.value = LoginUiState() }
}
