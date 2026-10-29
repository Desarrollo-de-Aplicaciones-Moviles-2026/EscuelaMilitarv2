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
fun CrearEquipoScreen(f: EquipoFormulario, evento: EventoModel?, datos: DatosApp, usuario: UsuarioModel,
                      accion: AccionUiState, cambiar: ((EquipoFormulario) -> EquipoFormulario) -> Unit, guardar: () -> Unit) {
    if (evento == null) { Pagina { Text("Competencia no encontrada.") }; return }
    val equipos = datos.equipos.filter { it.eventoId == evento.id }.map { it.id }.toSet()
    val inscritos = datos.integrantes.filter { it.equipoId in equipos }.map { it.usuarioId }.toSet()
    Pagina {
        Text("Crear mi equipo", style = MaterialTheme.typography.headlineSmall)
        Text(evento.nombre)
        Text("Compañía ${usuario.compania} · Mínimo ${evento.minimoIntegrantes} integrantes")
        CampoTexto(f.nombre, { v -> cambiar { it.copy(nombre = v) } }, "Nombre del equipo", f.errores.nombre, habilitado = !accion.cargando)
        TituloSeccion("Integrantes (${f.integrantes.size})")
        datos.usuarios.filter { it.rol == Rol.USUARIO && it.activo && it.compania == usuario.compania }.forEach { miembro ->
            val capitan = miembro.id == usuario.id
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = miembro.id in f.integrantes, enabled = !capitan && miembro.id !in inscritos && !accion.cargando,
                    onCheckedChange = { checked -> cambiar { it.copy(integrantes = if (checked) it.integrantes + miembro.id else it.integrantes - miembro.id) } })
                Text(miembro.nombre + if (capitan) " · Capitán" else if (miembro.id in inscritos) " · Ya inscrito" else "", Modifier.weight(1f))
            }
        }
        MensajeError(f.errores.integrantes)
        Text("El capitán integra siempre el equipo. La inscripción no consume tokens personales.")
        MensajeError(accion.error)
        BotonPersonalizado("Inscribir equipo", guardar, cargando = accion.cargando)
    }
}
