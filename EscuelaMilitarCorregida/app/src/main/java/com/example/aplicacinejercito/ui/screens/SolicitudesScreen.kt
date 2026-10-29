package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun SolicitudesScreen(datos: DatosApp, detalle: (String) -> Unit) {
    var filtro by rememberSaveable { mutableStateOf("Pendiente") }
    var grado by rememberSaveable { mutableStateOf("Todos") }
    val reservas = datos.reservas.filter { r -> r.estado.etiqueta == filtro && (grado == "Todos" || datos.usuarios.firstOrNull { it.id == r.usuarioId }?.grado?.etiqueta == grado) }
    Pagina {
        Text("Solicitudes", style = MaterialTheme.typography.headlineSmall)
        Selector("Estado", filtro, listOf("Pendiente", "Confirmada", "Rechazada", "Cancelada"), { filtro = it })
        Selector("Grado", grado, listOf("Todos") + Grado.entries.map { it.etiqueta }, { grado = it })
        Text("${reservas.size} solicitudes")
        reservas.forEach { TarjetaReserva(it, datos, { detalle(it.id) }, mostrarUsuario = true) }
        if (reservas.isEmpty()) Text("No hay solicitudes para este filtro.")
    }
}
