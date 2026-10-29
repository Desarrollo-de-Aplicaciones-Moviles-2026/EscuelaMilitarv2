package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.aplicacinejercito.data.DatosDemostracion
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun InstalacionesScreen(datos: DatosApp, nueva: () -> Unit, detalle: (String) -> Unit) {
    var filtro by rememberSaveable { mutableStateOf("Activas") }
    var estado by rememberSaveable { mutableStateOf("Todos") }
    var buscar by rememberSaveable { mutableStateOf("") }
    val canchas = datos.canchas.filter { it.activa == (filtro != "Archivadas") && it.nombre.contains(buscar, ignoreCase = true) && (estado == "Todos" || it.estado.etiqueta == estado) }
    Pagina {
        Text("Instalaciones", style = MaterialTheme.typography.headlineSmall)
        BotonPersonalizado("Nueva instalación", nueva)
        CampoTexto(buscar, { buscar = it }, "Buscar por nombre")
        Selector("Mostrar", filtro, listOf("Activas", "Archivadas"), { filtro = it })
        Selector("Estado de la instalación", estado, listOf("Todos") + EstadoCancha.entries.map { it.etiqueta }, { estado = it })
        Text("${canchas.size} instalaciones", style = MaterialTheme.typography.bodySmall)
        canchas.forEach { TarjetaCancha(it, alDetalle = { detalle(it.id) }) }
        if (canchas.isEmpty()) Text("No hay instalaciones para este filtro.")
    }
}
