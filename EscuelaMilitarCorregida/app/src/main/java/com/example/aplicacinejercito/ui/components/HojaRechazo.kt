package com.example.aplicacinejercito.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*

// Menú inferior que aparece al presionar Rechazar en la bandeja de Solicitudes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaRechazo(cantidad: Int, formulario: RechazoFormulario, cargando: Boolean,
                cambiar: ((RechazoFormulario) -> RechazoFormulario) -> Unit,
                cancelar: () -> Unit, confirmar: () -> Unit) {
    ModalBottomSheet(onDismissRequest = cancelar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        ContenidoRechazo(cantidad, formulario, cargando, cambiar, cancelar, confirmar)
    }
}

@Composable
fun ContenidoRechazo(cantidad: Int, formulario: RechazoFormulario, cargando: Boolean,
                     cambiar: ((RechazoFormulario) -> RechazoFormulario) -> Unit,
                     cancelar: () -> Unit, confirmar: () -> Unit) {
    val titulo = if (cantidad == 1) "Rechazar 1 solicitud" else "Rechazar $cantidad solicitudes"
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.size(48.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.EventBusy, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Se avisará a cada usuario con el motivo y se le devolverá su token.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("MOTIVO DEL RECHAZO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        MotivoRechazo.entries.forEach { motivo ->
            OpcionMotivo(motivo.etiqueta, formulario.motivo == motivo) {
                cambiar { it.copy(motivo = motivo, error = null) }
            }
        }
        if (formulario.motivo == MotivoRechazo.OTRO) {
            Text("Escribe el motivo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = formulario.otro,
                onValueChange = { texto -> cambiar { it.copy(otro = texto.take(SolicitudesModel.LIMITE_MOTIVO), error = null) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = formulario.error != null,
                enabled = !cargando,
                placeholder = { Text("Ej.: la cancha se usará para una revista de tropas") },
                supportingText = { Text("${formulario.otro.length}/${SolicitudesModel.LIMITE_MOTIVO}") }
            )
        }
        formulario.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = cancelar, enabled = !cargando, shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(onClick = confirmar, enabled = !cargando, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError)) { Text("Rechazar") }
        }
    }
}

@Composable
private fun OpcionMotivo(etiqueta: String, seleccionada: Boolean, alSeleccionar: () -> Unit) {
    val forma = MaterialTheme.shapes.medium
    Row(
        Modifier.fillMaxWidth().clip(forma)
            .background(if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, forma)
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = alSeleccionar)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
    }
}
