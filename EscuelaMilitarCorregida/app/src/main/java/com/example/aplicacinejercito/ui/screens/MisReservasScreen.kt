package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun MisReservasScreen(datos: DatosApp, usuarioId: String, detalle: (String) -> Unit, historial: Boolean = false) {
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    val reservas = datos.reservas.filter { it.usuarioId == usuarioId && (historial || Fechas.futuro(it.fecha, it.horaInicio)) }
        .filter { filtro == "Todas" || it.estado.etiqueta == filtro }
    Pagina {
        Text(if (historial) "Historial de reservas" else "Mis reservas", style = MaterialTheme.typography.headlineSmall)
        Selector("Estado", filtro, listOf("Todas") + EstadoReserva.entries.map { it.etiqueta }, { filtro = it })
        if (reservas.isEmpty()) Text("No hay reservas para este filtro.")
        reservas.forEach { TarjetaReserva(it, datos, { detalle(it.id) }) }
    }
}
