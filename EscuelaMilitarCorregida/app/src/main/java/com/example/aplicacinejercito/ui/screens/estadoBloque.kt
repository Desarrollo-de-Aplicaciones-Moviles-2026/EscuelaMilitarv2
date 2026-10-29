package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

internal fun estadoBloque(cancha: CanchaModel, f: ReservaFormulario, datos: DatosApp): String = when {
    !cancha.activa -> "Archivada"
    cancha.estado != EstadoCancha.HABILITADA -> cancha.estado.etiqueta
    !cancha.permite(f.fecha, f.horaInicio) -> "Cerrada en este bloque"
    !Fechas.futuro(f.fecha, f.horaInicio) -> "Bloque finalizado"
    ReglasReserva.bloqueadoPorEvento(cancha.id, f.fecha, f.horaInicio, datos.eventos, datos.eventoCanchas) -> "Evento institucional"
    datos.reservas.any { it.canchaId == cancha.id && it.fecha == f.fecha && it.horaInicio == f.horaInicio && it.ocupaHorario } -> "Ocupada"
    else -> "Disponible"
}
