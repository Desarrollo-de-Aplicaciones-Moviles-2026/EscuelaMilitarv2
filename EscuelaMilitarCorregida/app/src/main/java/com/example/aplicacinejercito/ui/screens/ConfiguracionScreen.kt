package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun ConfiguracionScreen(perfil: () -> Unit, politicas: () -> Unit, usuarios: () -> Unit,
                        reportes: () -> Unit, calendario: () -> Unit, notificaciones: () -> Unit, preferencias: () -> Unit) {
    Pagina {
        Text("Configuración", style = MaterialTheme.typography.headlineSmall)
        listOf("Mi perfil" to perfil, "Preferencias y notificaciones" to preferencias, "Políticas de reserva" to politicas, "Usuarios" to usuarios,
            "Reportes" to reportes, "Calendario institucional" to calendario, "Notificaciones" to notificaciones).forEach { (titulo, accion) ->
            OutlinedButton(onClick = accion, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(titulo) }
        }
    }
}
