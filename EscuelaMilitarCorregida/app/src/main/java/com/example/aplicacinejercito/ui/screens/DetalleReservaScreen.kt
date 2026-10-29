package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun DetalleReservaScreen(reserva: ReservaModel?, datos: DatosApp, admin: Boolean, ocupada: Boolean,
                         cancelar: () -> Unit, aprobar: () -> Unit = {}, rechazar: () -> Unit = {}, modificar: () -> Unit = {}) {
    var confirmarCancelacion by rememberSaveable { mutableStateOf(false) }
    if (reserva == null) { Pagina { Text("La reserva no está disponible.") }; return }
    val cancha = datos.canchas.firstOrNull { it.id == reserva.canchaId }
    val futuro = Fechas.futuro(reserva.fecha, reserva.horaInicio)
    Pagina {
        TarjetaContenido {
            IconoInstalacion(cancha?.tipo.orEmpty())
            Text(cancha?.nombre ?: "Instalación archivada", style = MaterialTheme.typography.titleLarge)
            EtiquetaEstado(reserva.estado.etiqueta)
            FilaDato("Fecha", Fechas.etiqueta(reserva.fecha))
            FilaDato("Horario", Fechas.horario(reserva.horaInicio))
            if (admin) FilaDato("Titular", datos.usuarios.firstOrNull { it.id == reserva.usuarioId }?.nombre.orEmpty())
        }
        if (admin && reserva.estado == EstadoReserva.PENDIENTE) {
            BotonPersonalizado("Aprobar solicitud", aprobar, habilitado = futuro && !ocupada)
            OutlinedButton(onClick = rechazar, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text("Rechazar solicitud") }
        }
        if (reserva.estado == EstadoReserva.PENDIENTE && !admin) Text("La administración está revisando tu solicitud. El bloque permanece apartado.")
        if (futuro && reserva.estado == EstadoReserva.CONFIRMADA) {
            if (admin) OutlinedButton(onClick = modificar, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text("Modificar fecha y hora") }
            OutlinedButton(onClick = { confirmarCancelacion = true }, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text("Cancelar reserva") }
            BotonCalendario(reserva, cancha?.nombre.orEmpty())
        }
    }
    if (confirmarCancelacion) ConfirmarDialogo("Cancelar reserva", "Se liberará el bloque y se revisará la lista de espera.", cancelar, { confirmarCancelacion = false })
}
