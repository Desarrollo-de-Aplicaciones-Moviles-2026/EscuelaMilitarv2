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
fun CrearEventoScreen(f: EventoFormulario, datos: DatosApp, accion: AccionUiState,
                      cambiar: ((EventoFormulario) -> EventoFormulario) -> Unit, guardar: () -> Unit) {
    Pagina {
        Text("Nuevo evento", style = MaterialTheme.typography.headlineSmall)
        CampoTexto(f.nombre, { v -> cambiar { it.copy(nombre = v) } }, "Nombre", f.errores.nombre, habilitado = !accion.cargando)
        Selector("Disciplina", f.disciplina, listOf("Fútbol", "Atletismo", "Básquetbol", "Vóleibol", "Tenis", "Rugby", "Otra"), { v -> cambiar { it.copy(disciplina = v) } }, !accion.cargando)
        CampoFecha(f.fecha, { v -> cambiar { it.copy(fecha = v) } }, f.errores.fecha, !accion.cargando)
        CampoTexto(f.inicio, { v -> cambiar { it.copy(inicio = v) } }, "Hora de inicio de 0 a 23", teclado = KeyboardType.Number, habilitado = !accion.cargando)
        CampoTexto(f.fin, { v -> cambiar { it.copy(fin = v) } }, "Hora de término de 1 a 24", f.errores.horario, KeyboardType.Number, habilitado = !accion.cargando)
        CampoTexto(f.cupos, { v -> cambiar { it.copy(cupos = v) } }, "Cupo de equipos", f.errores.equipos, KeyboardType.Number, habilitado = !accion.cargando)
        CampoTexto(f.minimo, { v -> cambiar { it.copy(minimo = v) } }, "Mínimo de integrantes por equipo de 1 a 8", teclado = KeyboardType.Number, habilitado = !accion.cargando)
        CampoTexto(f.descripcion, { v -> cambiar { it.copy(descripcion = v) } }, "Descripción", variasLineas = true, habilitado = !accion.cargando)
        TituloSeccion("Instalaciones")
        datos.canchas.filter { it.activa && it.estado == EstadoCancha.HABILITADA }.forEach { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = c.id in f.canchas, enabled = !accion.cargando, onCheckedChange = { checked -> cambiar { it.copy(canchas = if (checked) it.canchas + c.id else it.canchas - c.id) } })
                Text(c.nombre, Modifier.weight(1f))
            }
        }
        MensajeError(f.errores.instalaciones)
        Text("Se bloquearán todas las instalaciones seleccionadas durante el intervalo del evento. No puede haber reservas o eventos superpuestos.")
        MensajeError(accion.error)
        BotonPersonalizado("Crear evento", guardar, cargando = accion.cargando)
    }
}
