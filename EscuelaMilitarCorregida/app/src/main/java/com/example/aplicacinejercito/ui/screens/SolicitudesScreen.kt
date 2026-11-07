package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun SolicitudesScreen(datos: DatosApp, filtros: SolicitudesUiState, cargando: Boolean,
                      cambiar: ((SolicitudesUiState) -> SolicitudesUiState) -> Unit,
                      aprobar: () -> Unit, abrirRechazo: () -> Unit,
                      cambiarRechazo: ((RechazoFormulario) -> RechazoFormulario) -> Unit,
                      cerrarRechazo: () -> Unit, confirmarRechazo: () -> Unit) {
    val reservas = SolicitudesModel.filtrar(datos, filtros)
    val seleccionables = SolicitudesModel.seleccionables(reservas)
    val marcadas = filtros.seleccionadas.intersect(seleccionables)
    val todasMarcadas = seleccionables.isNotEmpty() && marcadas.size == seleccionables.size
    val nPendientes = SolicitudesModel.contarPendientes(datos)
    val nProcesadas = SolicitudesModel.contarProcesadas(datos)
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            Pagina {
                ControlSegmentado(listOf("Por revisar ($nPendientes)", "Procesadas ($nProcesadas)"),
                    if (filtros.procesadas) 1 else 0,
                    { indice -> cambiar { it.copy(procesadas = indice == 1, seleccionadas = emptySet()) } })
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SolicitudesModel.contarPorGrado(datos, filtros.procesadas).forEach { conteo ->
                        FiltroGrado("${conteo.etiqueta} (${conteo.cantidad})", activo = filtros.grado == conteo.clave,
                            alClick = { cambiar { it.copy(grado = conteo.clave, seleccionadas = emptySet()) } })
                    }
                }
                if (!filtros.procesadas) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${marcadas.size} seleccionadas", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (todasMarcadas) "Quitar selección" else "Seleccionar todas",
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable(enabled = seleccionables.isNotEmpty()) {
                                cambiar { it.copy(seleccionadas = if (todasMarcadas) emptySet() else seleccionables) }
                            })
                    }
                }
                reservas.forEach { reserva ->
                    TarjetaSolicitud(reserva, datos,
                        seleccionable = reserva.id in seleccionables,
                        seleccionada = reserva.id in marcadas,
                        alSeleccionar = {
                            cambiar { it.copy(seleccionadas = if (reserva.id in it.seleccionadas) it.seleccionadas - reserva.id else it.seleccionadas + reserva.id) }
                        })
                }
                if (reservas.isEmpty()) Text(if (filtros.procesadas) "No hay solicitudes procesadas para este filtro." else "No hay solicitudes por revisar.")
            }
        }
        if (!filtros.procesadas) BarraAccionesLote(habilitado = marcadas.isNotEmpty() && !cargando,
            alRechazar = abrirRechazo, alAprobar = aprobar)
    }
    filtros.rechazo?.let { formulario ->
        HojaRechazo(marcadas.size, formulario, cargando, cambiarRechazo, cerrarRechazo, confirmarRechazo)
    }
}
