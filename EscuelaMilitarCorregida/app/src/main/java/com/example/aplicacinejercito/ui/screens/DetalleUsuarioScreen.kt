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
fun DetalleUsuarioScreen(usuario: UsuarioModel?, datos: DatosApp, ocupada: Boolean, cambiarEstado: (Boolean) -> Unit, reserva: (String) -> Unit) {
    var confirmar by rememberSaveable { mutableStateOf(false) }
    if (usuario == null) { Pagina { Text("Usuario no encontrado.") }; return }
    Pagina {
        Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall)
        Text("${usuario.grado.etiqueta} · Compañía ${usuario.compania}")
        Text(usuario.correo)
        EtiquetaEstado(if (usuario.activo) "Activa" else "Archivada")
        val politica = datos.politicas.firstOrNull { it.grado == usuario.grado }
        Text("Tokens usados esta semana ${ReglasReserva.tokensUsados(datos.reservas, usuario.id, Fechas.hoy())} de ${politica?.tokensSemana ?: 0}")
        OutlinedButton(onClick = { confirmar = true }, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text(if (usuario.activo) "Archivar cuenta" else "Restaurar cuenta") }
        TituloSeccion("Historial de reservas")
        val reservas = datos.reservas.filter { it.usuarioId == usuario.id }
        if (reservas.isEmpty()) Text("No hay reservas registradas.")
        reservas.forEach { TarjetaReserva(it, datos, { reserva(it.id) }) }
    }
    if (confirmar) ConfirmarDialogo(if (usuario.activo) "Archivar cuenta" else "Restaurar cuenta",
        if (usuario.activo) "La cuenta perderá el acceso y sus reservas futuras se cancelarán. Su historial y sus equipos registrados se conservarán." else "La cuenta volverá a tener acceso.",
        { cambiarEstado(!usuario.activo) }, { confirmar = false })
}
