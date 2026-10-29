package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun ResultadosScreen(f: ReservaFormulario, datos: DatosApp, detalle: (String) -> Unit, cambiarBusqueda: () -> Unit) {
    val resultados = datos.canchas.filter { it.activa && (f.tipo == "Todos" || it.tipo == f.tipo) }
    Pagina {
        Text("Paso 2 de 3", style = MaterialTheme.typography.labelLarge)
        Text("${Fechas.etiqueta(f.fecha)} · ${Fechas.horario(f.horaInicio)}")
        Text("${resultados.size} instalaciones encontradas")
        TextButton(onClick = cambiarBusqueda) { Text("Cambiar búsqueda") }
        resultados.forEach { TarjetaCancha(it, estadoBloque(it, f, datos), { detalle(it.id) }) }
        if (resultados.isEmpty()) Text("No hay instalaciones de este tipo.")
    }
}
