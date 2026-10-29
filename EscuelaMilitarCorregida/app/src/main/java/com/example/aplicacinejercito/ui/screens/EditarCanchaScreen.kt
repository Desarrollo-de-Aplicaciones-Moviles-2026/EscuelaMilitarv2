package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.data.DatosDemostracion
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun EditarCanchaScreen(f: CanchaFormulario, accion: AccionUiState,
                      cambiar: ((CanchaFormulario) -> CanchaFormulario) -> Unit, guardar: () -> Unit) {
    val apertura = f.apertura.toIntOrNull() ?: 8
    val cierre = f.cierre.toIntOrNull() ?: 18
    val jornada = (apertura until cierre).toList()
    Pagina {
        TituloSeccion(if (f.id.isBlank()) "Nueva instalación" else "Datos de la instalación")
        TarjetaContenido {
            CampoTexto(f.nombre, { valor -> cambiar { it.copy(nombre = valor) } }, "Nombre", f.errores.nombre, habilitado = !accion.cargando)
            Selector("Tipo", f.tipo, (DatosDemostracion.tipos + f.tipo).distinct().sorted(), { valor -> cambiar { it.copy(tipo = valor) } }, !accion.cargando)
            CampoTexto(f.capacidad, { valor -> cambiar { it.copy(capacidad = valor) } }, "Capacidad de la instalación", f.errores.capacidad, KeyboardType.Number, habilitado = !accion.cargando)
        }
        TituloSeccion("Días habilitados para reservar")
        val dias = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
        dias.chunked(4).forEachIndexed { fila, nombres ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                nombres.forEachIndexed { col, nombre ->
                    val dia = fila * 4 + col + 1
                    FilterChip(selected = dia in f.dias, enabled = !accion.cargando, modifier = Modifier.weight(1f),
                        onClick = { cambiar { it.copy(dias = if (dia in it.dias) it.dias - dia else it.dias + dia) } },
                        label = { Text(nombre) }, colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                }
                repeat(4 - nombres.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        TituloSeccion("Jornada de reserva")
        Text("Por defecto, 08:00 a 18:00. Puedes ampliar o reducir la jornada y habilitar bloques específicos.", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) { Selector("Apertura", "%02d:00".format(apertura), (0..23).map { "%02d:00".format(it) }, { valor ->
                val nueva = valor.substringBefore(':').toInt()
                cambiar { anterior ->
                    val fin = maxOf(nueva + 1, anterior.cierre.toIntOrNull() ?: 18)
                    anterior.copy(apertura = nueva.toString(), cierre = fin.toString(), bloques = (nueva until fin).toSet())
                }
            }, !accion.cargando) }
            Column(Modifier.weight(1f)) { Selector("Cierre", "%02d:00".format(cierre), ((apertura + 1)..24).map { "%02d:00".format(it) }, { valor ->
                val nueva = valor.substringBefore(':').toInt()
                cambiar { it.copy(cierre = nueva.toString(), bloques = (apertura until nueva).toSet()) }
            }, !accion.cargando) }
        }
        TituloSeccion("Bloques disponibles · ${f.bloques.size} de ${jornada.size}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { cambiar { it.copy(bloques = jornada.toSet()) } }, enabled = !accion.cargando) { Text("Todos") }
            TextButton(onClick = { cambiar { it.copy(bloques = jornada.filter { h -> h < 13 }.toSet()) } }, enabled = !accion.cargando) { Text("Mañana") }
            TextButton(onClick = { cambiar { it.copy(bloques = jornada.filter { h -> h >= 13 }.toSet()) } }, enabled = !accion.cargando) { Text("Tarde") }
        }
        BloquesHorarios(jornada, f.bloques, { hora -> cambiar { it.copy(bloques = if (hora in it.bloques) it.bloques - hora else it.bloques + hora) } }, habilitado = { !accion.cargando }, varias = true)
        MensajeError(f.errores.horario)
        Selector("Estado de la instalación", f.estado.etiqueta, EstadoCancha.entries.map { it.etiqueta }, { valor -> cambiar { it.copy(estado = EstadoCancha.entries.first { e -> e.etiqueta == valor }) } }, !accion.cargando)
        CampoTexto(f.observaciones, { valor -> cambiar { it.copy(observaciones = valor) } }, "Indicaciones de uso", variasLineas = true, habilitado = !accion.cargando)
        Text("Los cambios de horario se guardan si respetan las reservas y eventos futuros de esta instalación.", style = MaterialTheme.typography.bodySmall)
        MensajeError(accion.error)
        BotonPersonalizado("Guardar instalación y horarios", guardar, cargando = accion.cargando)
    }
}
