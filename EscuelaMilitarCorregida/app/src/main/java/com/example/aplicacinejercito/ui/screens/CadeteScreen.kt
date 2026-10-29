package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun CadeteScreen(estado: AppUiState, nuevaReserva: () -> Unit, verReserva: (String) -> Unit, competencias: () -> Unit,
                 verEvento: (String) -> Unit, misReservas: () -> Unit) {
    val usuario = estado.usuario ?: return
    val politica = estado.datos.politicas.firstOrNull { it.grado == usuario.grado } ?: return
    val usados = ReglasReserva.tokensUsados(estado.datos.reservas, usuario.id, Fechas.hoy())
    val disponibles = (politica.tokensSemana - usados).coerceAtLeast(0)
    val proxima = estado.datos.reservas.filter { it.usuarioId == usuario.id && it.ocupaHorario && Fechas.futuro(it.fecha, it.horaInicio) }.minWithOrNull(compareBy({ it.fecha }, { it.horaInicio }))
    val eventos = estado.datos.eventos.filter { it.activo && Fechas.futuro(it.fecha, it.horaInicio) }.sortedWith(compareBy({ it.fecha }, { it.horaInicio })).take(3)
    Pagina {
        TarjetaContenido {
            Text("TOKENS SEMANALES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tienes $disponibles de ${politica.tokensSemana} tokens disponibles esta semana", style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(progress = { disponibles.toFloat() / politica.tokensSemana.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Text("Cada solicitud consume 1 token. Se renuevan cada lunes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = nuevaReserva, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) {
            Icon(Icons.Default.AddCircleOutline, null); Spacer(Modifier.width(8.dp)); Text("Nueva reserva")
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("PRÓXIMA RESERVA", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = misReservas) { Text("Ver todas", style = MaterialTheme.typography.labelMedium) }
        }
        if (proxima == null) TarjetaContenido { Text("No tienes reservas próximas. Busca una instalación para comenzar.") }
        else TarjetaReserva(proxima, estado.datos, { verReserva(proxima.id) })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("PRÓXIMOS EVENTOS", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = competencias) { Text("Ver todos", style = MaterialTheme.typography.labelMedium) }
        }
        if (eventos.isEmpty()) TarjetaContenido { Text("Aún no hay eventos próximos publicados.") }
        eventos.forEach { e -> TarjetaEventoProximo(e, estado.datos, { verEvento(e.id) }) }
    }
}
