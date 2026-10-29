package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.aplicacinejercito.data.DatosDemostracion
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun FichaCanchaScreen(cancha: CanchaModel?, datos: DatosApp, editar: () -> Unit,
                     archivar: (Boolean) -> Unit, reserva: (String) -> Unit, ocupada: Boolean) {
    var confirmarArchivo by rememberSaveable { mutableStateOf(false) }
    if (cancha == null) { Pagina { Text("Instalación no encontrada.") }; return }
    Pagina {
        Text(cancha.nombre, style = MaterialTheme.typography.headlineSmall)
        EtiquetaEstado(if (cancha.activa) cancha.estado.etiqueta else "Archivada")
        IconoInstalacion(cancha.tipo)
        Text("${cancha.tipo} · Capacidad ${cancha.capacidad} personas")
        ResumenHorarios(cancha)
        if (cancha.observaciones.isNotBlank()) Text(cancha.observaciones)
        BotonPersonalizado("Editar datos y horarios", editar, habilitado = !ocupada)
        OutlinedButton(onClick = { confirmarArchivo = true }, enabled = !ocupada, modifier = Modifier.fillMaxWidth()) { Text(if (cancha.activa) "Archivar instalación" else "Restaurar instalación") }
        TituloSeccion("Reservas e historial de uso")
        val reservas = datos.reservas.filter { it.canchaId == cancha.id }
        if (reservas.isEmpty()) Text("No hay reservas registradas en esta instalación.")
        reservas.forEach { TarjetaReserva(it, datos, { reserva(it.id) }, mostrarUsuario = true) }
        TituloSeccion("Eventos asociados")
        val eventos = datos.eventos.filter { e -> e.activo && datos.eventoCanchas.any { it.eventoId == e.id && it.canchaId == cancha.id } }
        eventos.forEach { Text("${Fechas.etiqueta(it.fecha)} · ${it.horaInicio}:00 a ${it.horaFin}:00 · ${it.nombre}") }
        if (eventos.isEmpty()) Text("No hay eventos activos asociados.")
    }
    if (confirmarArchivo) ConfirmarDialogo(if (cancha.activa) "Archivar instalación" else "Restaurar instalación",
        if (cancha.activa) "Se conservará el historial. Primero debes cancelar o reprogramar sus reservas y eventos futuros." else "La instalación volverá a aparecer en las búsquedas según su estado y horario.",
        { archivar(cancha.activa) }, { confirmarArchivo = false })
}
