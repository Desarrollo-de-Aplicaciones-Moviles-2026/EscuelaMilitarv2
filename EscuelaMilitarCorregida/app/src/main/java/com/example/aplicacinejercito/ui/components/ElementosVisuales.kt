package com.example.aplicacinejercito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import java.time.format.DateTimeFormatter
import java.util.Locale

fun iconoInstalacion(tipo: String): ImageVector = when {
    tipo.contains("atlet", true) -> Icons.AutoMirrored.Filled.DirectionsRun
    tipo.contains("muscul", true) -> Icons.Default.FitnessCenter
    tipo.contains("básquet", true) -> Icons.Default.SportsBasketball
    tipo.contains("vólei", true) -> Icons.Default.SportsVolleyball
    tipo.contains("tenis", true) -> Icons.Default.SportsTennis
    tipo.contains("rugby", true) -> Icons.Default.SportsRugby
    tipo.contains("fútbol", true) || tipo.contains("futbol", true) -> Icons.Default.SportsSoccer
    tipo.contains("tatami", true) || tipo.contains("combate", true) -> Icons.Default.SportsMartialArts
    tipo.contains("auditorio", true) || tipo.contains("magna", true) -> Icons.Default.AccountBalance
    tipo.contains("pentatl", true) -> Icons.Default.MilitaryTech
    tipo.contains("estadio", true) -> Icons.Default.Stadium
    else -> Icons.Default.Sports
}

@Composable
fun IconoInstalacion(tipo: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
        Icon(iconoInstalacion(tipo), null, Modifier.padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun TarjetaContenido(contenido: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, Modifier.weight(0.4f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, Modifier.weight(0.6f), fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun BloquesHorarios(horas: List<Int>, seleccionados: Set<Int>, cambiar: (Int) -> Unit,
                    habilitado: (Int) -> Boolean = { true }, varias: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        horas.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { hora ->
                    val elegido = hora in seleccionados
                    val activo = habilitado(hora)
                    val colores = if (varias) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary
                    FilterChip(selected = elegido, onClick = { cambiar(hora) }, enabled = activo,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        label = { Text(Fechas.horario(hora), style = MaterialTheme.typography.labelMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colores,
                            selectedLabelColor = if (varias) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                }
                repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun FechaCompacta(fecha: String, destacada: Boolean = false) {
    val dia = Fechas.leer(fecha)
    Surface(color = if (destacada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (destacada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium) {
        Column(Modifier.width(52.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dia?.format(DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("es-CL")))?.uppercase().orEmpty(), style = MaterialTheme.typography.labelSmall)
            Text(dia?.dayOfMonth?.toString().orEmpty(), style = MaterialTheme.typography.titleMedium)
            Text(dia?.format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-CL")))?.uppercase().orEmpty(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun TarjetaEventoProximo(evento: EventoModel, datos: DatosApp, abrir: () -> Unit) {
    OutlinedCard(onClick = abrir, modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FechaCompacta(evento.fecha)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(evento.nombre, style = MaterialTheme.typography.titleSmall)
                Text("%02d:00–%02d:00 · %s".format(evento.horaInicio, evento.horaFin, evento.disciplina), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val lugares = datos.eventoCanchas.filter { it.eventoId == evento.id }.mapNotNull { rel -> datos.canchas.firstOrNull { it.id == rel.canchaId }?.nombre }
                Text(lugares.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Ver evento e inscripciones", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
        }
    }
}

@Composable
fun ResumenHorarios(cancha: CanchaModel) {
    val dias = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
    Text(Horarios.dias(cancha.diasActivos).joinToString(" · ") { dias[it - 1] }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(Horarios.horas(cancha.bloquesDisponibles).sorted().joinToString(" · ") { "%02d:00".format(it) }.let { "Inicios disponibles: $it" },
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
