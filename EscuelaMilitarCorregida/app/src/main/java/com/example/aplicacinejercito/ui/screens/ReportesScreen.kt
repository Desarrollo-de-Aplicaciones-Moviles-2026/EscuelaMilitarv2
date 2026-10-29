package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun ReportesScreen(datos: DatosApp, filtro: ReporteFiltros, cambiar: ((ReporteFiltros) -> ReporteFiltros) -> Unit) {
    val hoy = Fechas.hoy()
    val rango = filtro.rango(hoy)
    val reservas = filtro.filtrar(datos.reservas, hoy)
    val colores = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onSurfaceVariant)
    Pagina {
        TarjetaContenido {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column { Text("Actividad de reservas", style = MaterialTheme.typography.titleLarge); Text("Datos registrados en este dispositivo", style = MaterialTheme.typography.bodySmall) }
            }
            Selector("Período", filtro.periodo.etiqueta, PeriodoReporte.entries.map { it.etiqueta }, { valor -> cambiar { it.copy(periodo = PeriodoReporte.entries.first { p -> p.etiqueta == valor }) } })
            if (filtro.periodo == PeriodoReporte.PERSONALIZADO) {
                // El análisis histórico necesita permitir fechas pasadas y también reservas futuras.
                CampoFecha(filtro.desde, { valor -> cambiar { it.copy(desde = valor) } }, minimo = null, etiqueta = "Desde")
                CampoFecha(filtro.hasta, { valor -> cambiar { it.copy(hasta = valor) } }, minimo = Fechas.leer(filtro.desde), etiqueta = "Hasta")
            }
            if (rango == null) MensajeError("Elige un intervalo válido; el fin debe ser igual o posterior al inicio.")
            else Text("${Fechas.etiqueta(rango.first.toString())} – ${Fechas.etiqueta(rango.second.toString())}", style = MaterialTheme.typography.labelMedium)
            Text("El filtro usa la fecha reservada para la actividad.", style = MaterialTheme.typography.bodySmall)
        }
        listOf("Total de solicitudes" to reservas.size, "Confirmadas" to reservas.count { it.estado == EstadoReserva.CONFIRMADA },
            "Pendientes" to reservas.count { it.estado == EstadoReserva.PENDIENTE }, "Titulares distintos" to reservas.map { it.usuarioId }.distinct().size).chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { (etiqueta, cantidad) ->
                    OutlinedCard(Modifier.weight(1f), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(cantidad.toString(), style = MaterialTheme.typography.headlineMedium)
                            Text(etiqueta, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        TarjetaContenido {
            TituloSeccion("Distribución por estado")
            if (reservas.isEmpty()) Text("Sin solicitudes en el período seleccionado.")
            else {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(164.dp).padding(12.dp).semantics { contentDescription = "Distribución de ${reservas.size} solicitudes; detalle por estado a continuación" }) {
                        var inicio = -90f
                        EstadoReserva.entries.forEachIndexed { i, estado ->
                            val angulo = 360f * reservas.count { it.estado == estado } / reservas.size
                            if (angulo > 0) drawArc(colores[i], inicio, angulo, useCenter = false, style = Stroke(width = 22.dp.toPx()))
                            inicio += angulo
                        }
                    }
                    Text(reservas.size.toString(), style = MaterialTheme.typography.headlineMedium)
                }
                EstadoReserva.entries.forEachIndexed { i, estado ->
                    val cantidad = reservas.count { it.estado == estado }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(10.dp).background(colores[i], CircleShape))
                        Text(estado.etiqueta, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("$cantidad · ${cantidad * 100 / reservas.size}%", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        TarjetaContenido {
            TituloSeccion("Instalaciones con más solicitudes")
            val ranking = reservas.groupingBy { it.canchaId }.eachCount().entries.sortedByDescending { it.value }.take(5)
            if (ranking.isEmpty()) Text("Sin datos para este período.")
            ranking.forEach { (id, cantidad) ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(datos.canchas.firstOrNull { it.id == id }?.nombre ?: "Instalación archivada", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(cantidad.toString(), style = MaterialTheme.typography.labelLarge)
                }
                LinearProgressIndicator(progress = { cantidad.toFloat() / ranking.first().value.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
            Text("Incluye todos los estados de solicitud. Estos valores representan cantidades, no porcentajes de ocupación.", style = MaterialTheme.typography.bodySmall)
        }
        if (rango != null) BotonExportar(datos.copy(reservas = reservas))
    }
}
