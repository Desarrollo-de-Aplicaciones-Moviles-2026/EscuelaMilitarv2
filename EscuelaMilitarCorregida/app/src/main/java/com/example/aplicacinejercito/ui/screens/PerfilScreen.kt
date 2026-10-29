package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun PerfilScreen(estado: AppUiState, preferencias: () -> Unit,
                 guardarFoto: (String) -> Unit, historial: () -> Unit, ayuda: () -> Unit, salir: () -> Unit, ocupada: Boolean = false) {
    val usuario = estado.usuario ?: return
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    Pagina {
        FotoPerfil(estado.preferencias.fotos[usuario.id], guardarFoto)
        Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall)
        Text(usuario.correo)
        Text(if (usuario.rol == Rol.ADMIN) "Administración" else "${usuario.grado.etiqueta} · Compañía ${usuario.compania}")
        if (usuario.rol == Rol.USUARIO) {
            val politica = estado.datos.politicas.firstOrNull { it.grado == usuario.grado }
            if (politica != null) Text("Tokens usados esta semana ${ReglasReserva.tokensUsados(estado.datos.reservas, usuario.id, Fechas.hoy())} de ${politica.tokensSemana}")
            OutlinedButton(onClick = historial, modifier = Modifier.fillMaxWidth()) { Text("Ver historial") }
        }
        OutlinedButton(onClick = preferencias, modifier = Modifier.fillMaxWidth()) { Text("Configuración y notificaciones") }
        OutlinedButton(onClick = ayuda, modifier = Modifier.fillMaxWidth()) { Text("Ayuda y reglas de uso") }
        OutlinedButton(onClick = { confirmarSalida = true }, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
    if (confirmarSalida) ConfirmarDialogo("Cerrar sesión", "Las reservas y preferencias seguirán guardadas en este dispositivo.", salir, { confirmarSalida = false }, "Salir")
}
