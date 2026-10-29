package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun ConfirmarReservaScreen(f: ReservaFormulario, cancha: CanchaModel?, politica: PoliticaModel, disponibles: Int, accion: AccionUiState,
                          cambiar: ((ReservaFormulario) -> ReservaFormulario) -> Unit, confirmar: () -> Unit) {
    Pagina {
        Text("Paso 3 de 3 · Revisa y envía", style = MaterialTheme.typography.labelLarge)
        TarjetaContenido {
            FilaDato("Instalación", cancha?.nombre ?: "Selecciona una instalación")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato("Tipo", cancha?.tipo.orEmpty())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato("Fecha", Fechas.etiqueta(f.fecha))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FilaDato("Horario", Fechas.horario(f.horaInicio))
        }
        TarjetaContenido {
            TituloSeccion("Tokens semanales")
            Text("Esta solicitud consume 1 token. Te quedarán ${(disponibles - 1).coerceAtLeast(0)} de ${politica.tokensSemana} esta semana.")
        }
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
            Text(if (politica.requiereAprobacion) "La administración revisará tu solicitud. El bloque quedará apartado hasta su aprobación o rechazo." else "La reserva se confirmará al enviar la solicitud.", Modifier.padding(16.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = f.aceptaNormas, onCheckedChange = { valor -> cambiar { it.copy(aceptaNormas = valor) } }, enabled = !accion.cargando)
            Text("Acepto las normas de uso y devolveré la instalación en buenas condiciones.", Modifier.weight(1f))
        }
        MensajeError(f.errores.normas)
        MensajeError(accion.error)
        BotonPersonalizado("Enviar solicitud", confirmar, cargando = accion.cargando, habilitado = cancha != null && disponibles > 0)
    }
}
