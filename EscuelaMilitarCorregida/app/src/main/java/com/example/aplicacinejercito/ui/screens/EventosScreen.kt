package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun EventosScreen(datos: DatosApp, usuario: UsuarioModel, detalle: (String) -> Unit, nuevo: () -> Unit = {}) {
    Pagina {
        Text("Eventos y competencias", style = MaterialTheme.typography.headlineSmall)
        if (usuario.rol == Rol.ADMIN) BotonPersonalizado("Crear evento o competencia", nuevo)
        if (datos.eventos.isEmpty()) Text("No hay eventos registrados.")
        datos.eventos.sortedWith(compareBy({ it.fecha }, { it.horaInicio })).forEach { e ->
            TarjetaEventoProximo(e, datos, { detalle(e.id) })
            EtiquetaEstado(if (!e.activo) "Cancelada" else if (Fechas.futuro(e.fecha, e.horaInicio)) "Inscripciones abiertas" else "Inscripciones cerradas")
        }
    }
}
