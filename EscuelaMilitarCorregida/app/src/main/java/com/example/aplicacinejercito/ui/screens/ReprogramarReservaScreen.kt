package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun ReprogramarReservaScreen(f: ReprogramacionFormulario, anticipacion: Int, accion: AccionUiState,
                            cambiar: ((ReprogramacionFormulario) -> ReprogramacionFormulario) -> Unit, guardar: () -> Unit) {
    Pagina {
        Text("Modificar reserva", style = MaterialTheme.typography.headlineSmall)
        CampoFecha(f.fecha, { v -> cambiar { it.copy(fecha = v) } }, f.errores.fecha, habilitado = !accion.cargando, maximo = Fechas.hoy().plusDays(anticipacion.toLong()))
        CampoTexto(f.hora, { v -> cambiar { it.copy(hora = v) } }, "Hora de inicio de 0 a 23", f.errores.hora, teclado = KeyboardType.Number, habilitado = !accion.cargando)
        Text("La modificación conserva el token de la solicitud original. Se comprueban nuevamente la disponibilidad y el plazo de anticipación del titular.")
        MensajeError(accion.error)
        BotonPersonalizado("Guardar nuevo bloque", guardar, cargando = accion.cargando)
    }
}
