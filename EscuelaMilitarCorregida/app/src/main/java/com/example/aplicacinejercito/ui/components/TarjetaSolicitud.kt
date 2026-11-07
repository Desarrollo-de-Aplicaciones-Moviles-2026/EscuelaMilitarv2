package com.example.aplicacinejercito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*

// Tocar la tarjeta o su checklist cambia la selección; las procesadas no responden.
@Composable
fun TarjetaSolicitud(reserva: ReservaModel, datos: DatosApp, seleccionable: Boolean, seleccionada: Boolean,
                     alSeleccionar: () -> Unit) {
    val titular = datos.usuarios.firstOrNull { it.id == reserva.usuarioId }
    val fondo = if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borde = if (seleccionada) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(enabled = seleccionable, onClick = alSeleccionar),
        colors = CardDefaults.outlinedCardColors(containerColor = fondo), border = borde) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (seleccionable) CasillaSolicitud(seleccionada, alSeleccionar)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(datos.canchas.firstOrNull { it.id == reserva.canchaId }?.nombre ?: "Instalación archivada",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.PersonOutline, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    InsigniaGrado(titular?.grado)
                    Text(titular?.nombre.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Fechas.etiqueta(reserva.fecha).replaceFirstChar { it.uppercase() } + " · " + Fechas.horario(reserva.horaInicio),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            EtiquetaEstado(reserva.estado.etiqueta, conPunto = true)
        }
    }
}
