package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun PreferenciasScreen(estado: AppUiState, cambiarTema: (Boolean) -> Unit, cambiarGeneral: (Boolean) -> Unit,
                       cambiarTipo: (TipoNotificacion, Boolean) -> Unit) {
    val id = estado.usuario?.id ?: return
    val prefs = estado.preferencias
    Pagina {
        TarjetaContenido {
            TituloSeccion("Apariencia")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tema oscuro", Modifier.weight(1f))
                Switch(checked = prefs.temaOscuro, onCheckedChange = cambiarTema)
            }
        }
        TarjetaContenido {
            TituloSeccion("Notificaciones de esta cuenta")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Activar notificaciones", Modifier.weight(1f))
                Switch(checked = prefs.generales(id), onCheckedChange = cambiarGeneral)
            }
            Text("Se muestran dentro de la aplicación. Tus elecciones de tipo se conservarán al desactivar la opción general.", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            TipoNotificacion.entries.forEach { tipo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tipo.etiqueta, Modifier.weight(1f))
                    Switch(checked = prefs.tipoActivo(id, tipo), enabled = prefs.generales(id), onCheckedChange = { cambiarTipo(tipo, it) })
                }
            }
        }
    }
}
