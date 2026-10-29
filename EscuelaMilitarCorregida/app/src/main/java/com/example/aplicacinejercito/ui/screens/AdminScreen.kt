package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*
import java.time.Instant

@Composable
fun AdminScreen(estado: AppUiState, solicitudes: () -> Unit, instalaciones: () -> Unit, verReserva: (String) -> Unit) {
    val ahora = Instant.ofEpochMilli(estado.ahora).atZone(Fechas.zona)
    val fecha = ahora.toLocalDate().toString()
    val activas = estado.datos.canchas.filter { it.activa }
    val ocupadas = activas.count { c -> c.estado == EstadoCancha.HABILITADA && (
        estado.datos.reservas.any { it.canchaId == c.id && it.fecha == fecha && it.horaInicio == ahora.hour && it.ocupaHorario } ||
            ReglasReserva.bloqueadoPorEvento(c.id, fecha, ahora.hour, estado.datos.eventos, estado.datos.eventoCanchas)) }
    val disponibles = activas.count { it.estado == EstadoCancha.HABILITADA && it.permite(fecha, ahora.hour) } - activas.count { c ->
        c.estado == EstadoCancha.HABILITADA && c.permite(fecha, ahora.hour) && (
            estado.datos.reservas.any { it.canchaId == c.id && it.fecha == fecha && it.horaInicio == ahora.hour && it.ocupaHorario } ||
                ReglasReserva.bloqueadoPorEvento(c.id, fecha, ahora.hour, estado.datos.eventos, estado.datos.eventoCanchas)) }
    val pendientes = estado.datos.reservas.count { it.estado == EstadoReserva.PENDIENTE }
    Pagina {
        Text("Administración", style = MaterialTheme.typography.headlineMedium)
        Text("${Fechas.etiqueta(fecha)} · Estado actual de las instalaciones")
        listOf("Disponibles ahora" to disponibles, "Ocupadas ahora" to ocupadas,
            "En mantenimiento" to activas.count { it.estado == EstadoCancha.MANTENIMIENTO },
            "No disponibles" to activas.count { it.estado == EstadoCancha.NO_DISPONIBLE }).chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { (titulo, numero) -> OutlinedCard(onClick = instalaciones, modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) { Text(numero.toString(), style = MaterialTheme.typography.headlineMedium); Text(titulo) }
                } }
            }
        }
        BotonPersonalizado("Solicitudes por revisar ($pendientes)", solicitudes)
        TituloSeccion("Actividades de hoy")
        val deHoy = estado.datos.reservas.filter { it.fecha == fecha && it.ocupaHorario }
        if (deHoy.isEmpty()) Text("No hay reservas activas para hoy.")
        deHoy.forEach { TarjetaReserva(it, estado.datos, { verReserva(it.id) }, mostrarUsuario = true) }
    }
}
