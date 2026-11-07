package com.example.aplicacinejercito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.Grado

// Control de dos pestañas con fondo gris y la opción activa en blanco.
@Composable
fun ControlSegmentado(opciones: List<String>, seleccion: Int, cambiar: (Int) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            opciones.forEachIndexed { indice, texto ->
                val activa = indice == seleccion
                Box(
                    Modifier.weight(1f).heightIn(min = 44.dp).clip(MaterialTheme.shapes.medium)
                        .background(if (activa) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .selectable(selected = activa, role = Role.Tab, onClick = { cambiar(indice) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(texto, style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (activa) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (activa) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// Píldora de filtro por grado: activa en azul institucional, inactiva en blanco con borde fino.
@Composable
fun FiltroGrado(texto: String, activo: Boolean, alClick: () -> Unit) {
    FilterChip(selected = activo, onClick = alClick, shape = CircleShape,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold) },
        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface, selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = activo,
            borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary))
}

// El área de toque mide 48dp para que sea fácil marcarla; el dibujo visible mide 24dp.
@Composable
fun CasillaSolicitud(marcada: Boolean, alCambiar: () -> Unit) {
    Box(Modifier.size(48.dp).toggleable(value = marcada, role = Role.Checkbox, onValueChange = { alCambiar() }),
        contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
            color = if (marcada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (marcada) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (marcada) Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun InsigniaGrado(grado: Grado?) {
    if (grado == null) return
    val (fondo, texto) = when (grado) {
        Grado.TENIENTE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        Grado.CABO -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurface
        Grado.CADETE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.small) {
        Text(grado.etiqueta.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

// Barra fija de acciones en lote: Rechazar (rojo) y Aprobar (azul institucional).
@Composable
fun BarraAccionesLote(habilitado: Boolean, alRechazar: () -> Unit, alAprobar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = alRechazar, enabled = habilitado, shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("Rechazar")
            }
            Button(onClick = alAprobar, enabled = habilitado, shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Aprobar")
            }
        }
    }
}
