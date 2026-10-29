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
fun UsuariosScreen(datos: DatosApp, detalle: (String) -> Unit) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Activos") }
    var grado by rememberSaveable { mutableStateOf("Todos") }
    val usuarios = datos.usuarios.filter { it.rol == Rol.USUARIO && it.activo == (filtro == "Activos") &&
        (grado == "Todos" || it.grado.etiqueta == grado) && (it.nombre.contains(consulta, true) || it.correo.contains(consulta, true)) }
    Pagina {
        Text("Usuarios", style = MaterialTheme.typography.headlineSmall)
        CampoTexto(consulta, { consulta = it }, "Buscar por nombre o correo")
        Selector("Estado de cuenta", filtro, listOf("Activos", "Archivados"), { filtro = it })
        Selector("Grado", grado, listOf("Todos") + Grado.entries.map { it.etiqueta }, { grado = it })
        usuarios.forEach { u ->
            OutlinedCard(onClick = { detalle(u.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) { Text(u.nombre, style = MaterialTheme.typography.titleMedium); Text("${u.grado.etiqueta} · ${u.compania}"); Text(u.correo, style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (usuarios.isEmpty()) Text("No hay usuarios para este filtro.")
    }
}
