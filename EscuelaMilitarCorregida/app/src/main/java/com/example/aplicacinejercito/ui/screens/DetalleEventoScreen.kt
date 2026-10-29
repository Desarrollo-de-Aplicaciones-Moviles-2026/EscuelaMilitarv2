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
fun DetalleEventoScreen(evento: EventoModel?, datos: DatosApp, usuario: UsuarioModel, ocupada: Boolean,
                        crearEquipo: () -> Unit, retirar: (EquipoModel) -> Unit, cancelar: () -> Unit) {
    var confirmarCancelacion by rememberSaveable { mutableStateOf(false) }
    var equipoRetirar by remember { mutableStateOf<EquipoModel?>(null) }
    if (evento == null) { Pagina { Text("Evento no encontrado.") }; return }
    val equipos = datos.equipos.filter { it.eventoId == evento.id }
    val inscrito = datos.integrantes.any { i -> i.usuarioId == usuario.id && equipos.any { it.id == i.equipoId } }
    val futuro = Fechas.futuro(evento.fecha, evento.horaInicio)
    Pagina {
        Text(evento.nombre, style = MaterialTheme.typography.headlineSmall)
        EtiquetaEstado(if (!evento.activo) "Cancelada" else if (futuro) "Inscripciones abiertas" else "Inscripciones cerradas")
        Text("${evento.disciplina}\n${Fechas.etiqueta(evento.fecha)} · ${evento.horaInicio}:00 a ${evento.horaFin}:00")
        if (evento.descripcion.isNotBlank()) Text(evento.descripcion)
        Text("Mínimo ${evento.minimoIntegrantes} integrantes por equipo · Cupo ${evento.cupoEquipos} equipos")
        TituloSeccion("Instalaciones reservadas para el evento")
        datos.eventoCanchas.filter { it.eventoId == evento.id }.forEach { rel -> Text(datos.canchas.firstOrNull { it.id == rel.canchaId }?.nombre.orEmpty()) }
        if (usuario.rol == Rol.USUARIO && futuro && evento.activo && !inscrito) BotonPersonalizado("Crear mi equipo", crearEquipo, habilitado = equipos.size < evento.cupoEquipos && !ocupada)
        if (inscrito) Text("Ya formas parte de un equipo inscrito en esta competencia.")
        TituloSeccion("Equipos inscritos (${equipos.size})")
        if (equipos.isEmpty()) Text("Aún no hay equipos inscritos.")
        equipos.forEach { equipo ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(equipo.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Compañía ${equipo.compania}")
                    Text("Capitán ${datos.usuarios.firstOrNull { it.id == equipo.capitanId }?.nombre.orEmpty()}")
                    datos.integrantes.filter { it.equipoId == equipo.id }.forEach { i -> Text(datos.usuarios.firstOrNull { it.id == i.usuarioId }?.nombre.orEmpty(), style = MaterialTheme.typography.bodySmall) }
                    if (futuro && (usuario.rol == Rol.ADMIN || equipo.capitanId == usuario.id)) TextButton(onClick = { equipoRetirar = equipo }, enabled = !ocupada) { Text("Retirar equipo") }
                }
            }
        }
        if (usuario.rol == Rol.ADMIN && evento.activo && futuro) OutlinedButton(onClick = { confirmarCancelacion = true }, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text("Cancelar evento") }
    }
    if (confirmarCancelacion) ConfirmarDialogo("Cancelar evento", "Se liberarán las instalaciones y se avisará a los capitanes de los equipos.", cancelar, { confirmarCancelacion = false })
    equipoRetirar?.let { ConfirmarDialogo("Retirar equipo", "Se eliminará la inscripción de ${it.nombre} de esta competencia.", { retirar(it) }, { equipoRetirar = null }) }
}
