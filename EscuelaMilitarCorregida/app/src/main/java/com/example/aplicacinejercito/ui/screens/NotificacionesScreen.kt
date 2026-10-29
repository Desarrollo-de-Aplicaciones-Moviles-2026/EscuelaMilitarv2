package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun NotificacionesScreen(estado: AppUiState, configurar: () -> Unit) {
    val id = estado.usuario?.id ?: return
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    val notificaciones = estado.datos.notificaciones.filter { it.usuarioId == id && estado.preferencias.permite(id, it.tipo) && (filtro == "Todas" || it.tipo.etiqueta == filtro) }
    Pagina {
        Selector("Filtrar por tipo", filtro, listOf("Todas") + TipoNotificacion.entries.map { it.etiqueta }, { filtro = it })
        TextButton(onClick = configurar) { Icon(Icons.Default.Tune, null); Spacer(Modifier.width(8.dp)); Text("Configurar notificaciones") }
        if (!estado.preferencias.generales(id)) TarjetaContenido { Text("Las notificaciones están desactivadas. Puedes activarlas en Configuración.") }
        else if (notificaciones.isEmpty()) TarjetaContenido { Text("No hay notificaciones visibles para este filtro.") }
        notificaciones.forEach { notificacion ->
            TarjetaContenido {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(when (notificacion.tipo) {
                        TipoNotificacion.SOLICITUDES -> Icons.AutoMirrored.Filled.Assignment
                        TipoNotificacion.RESERVAS -> Icons.Default.EventAvailable
                        TipoNotificacion.LISTA_ESPERA -> Icons.Default.HourglassEmpty
                        TipoNotificacion.EVENTOS -> Icons.Default.EmojiEvents
                        TipoNotificacion.CUENTA -> Icons.Default.Person
                    }, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(notificacion.tipo.etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(notificacion.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(notificacion.detalle, style = MaterialTheme.typography.bodyMedium)
                        Text(Instant.ofEpochMilli(notificacion.creadoEn).atZone(Fechas.zona).format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
