package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun CalendarioScreen(datos: DatosApp, detalle: (String) -> Unit, institucional: Boolean = false, anticipacion: Int? = null) {
    var fecha by rememberSaveable { mutableStateOf(Fechas.hoy().toString()) }
    Pagina {
        Text(if (institucional) "Calendario institucional" else "Calendario general", style = MaterialTheme.typography.headlineSmall)
        CampoFecha(fecha, { fecha = it }, maximo = anticipacion?.let { Fechas.hoy().plusDays(it.toLong()) })
        TituloSeccion("Eventos institucionales")
        val eventos = datos.eventos.filter { it.fecha == fecha && it.activo }
        if (eventos.isEmpty()) Text("No hay eventos para esta fecha.")
        eventos.forEach { e ->
            Text("${e.horaInicio}:00 a ${e.horaFin}:00 · ${e.nombre}", style = MaterialTheme.typography.titleMedium)
            Text(datos.eventoCanchas.filter { it.eventoId == e.id }.mapNotNull { rel -> datos.canchas.firstOrNull { it.id == rel.canchaId }?.nombre }.joinToString())
        }
        TituloSeccion("Reservas de instalaciones")
        val reservas = datos.reservas.filter { it.fecha == fecha && it.ocupaHorario }
        if (reservas.isEmpty()) Text("No hay reservas para esta fecha.")
        reservas.forEach { TarjetaReserva(it, datos, { detalle(it.id) }) }
        Text("El calendario muestra la ocupación local. Los detalles personales solo se abren para el titular o la administración.", style = MaterialTheme.typography.bodySmall)
    }
}
