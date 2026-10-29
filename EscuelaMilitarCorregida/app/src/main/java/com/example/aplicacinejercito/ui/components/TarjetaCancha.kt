package com.example.aplicacinejercito.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*

@Composable
fun EtiquetaEstado(texto: String) {
    val destino = when (texto) {
        "Disponible", "Confirmada", "Activa", "Inscripciones abiertas" -> MaterialTheme.colorScheme.tertiaryContainer
        "Rechazada", "Cancelada", "Ocupada", "No disponible" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val color by animateColorAsState(destino, label = "Color del estado")
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(texto, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium,
            color = when (texto) {
                "Disponible", "Confirmada", "Activa", "Inscripciones abiertas" -> MaterialTheme.colorScheme.onTertiaryContainer
                "Rechazada", "Cancelada", "Ocupada", "No disponible" -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            })
    }
}

@Composable
fun TarjetaCancha(cancha: CanchaModel, estado: String = cancha.estado.etiqueta, alDetalle: () -> Unit) {
    OutlinedCard(onClick = alDetalle, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoInstalacion(cancha.tipo)
                Column(Modifier.weight(1f)) {
                    Text(cancha.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(cancha.tipo, style = MaterialTheme.typography.bodySmall)
                }
            }
            EtiquetaEstado(if (cancha.activa) estado else "Archivada")
            ResumenHorarios(cancha)
            Text("Ver detalle", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun TarjetaReserva(reserva: ReservaModel, datos: DatosApp, alDetalle: () -> Unit, mostrarUsuario: Boolean = false) {
    OutlinedCard(onClick = alDetalle, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            FechaCompacta(reserva.fecha, destacada = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(datos.canchas.firstOrNull { it.id == reserva.canchaId }?.nombre ?: "Instalación archivada", style = MaterialTheme.typography.titleMedium)
                Text(Fechas.horario(reserva.horaInicio), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (mostrarUsuario) Text(datos.usuarios.firstOrNull { it.id == reserva.usuarioId }?.nombre.orEmpty(), style = MaterialTheme.typography.bodySmall)
                EtiquetaEstado(reserva.estado.etiqueta)
            }
        }
    }
}
