package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun InstalacionUsuarioScreen(cancha: CanchaModel?, f: ReservaFormulario, datos: DatosApp, usuarioId: String,
                             reservar: () -> Unit, espera: () -> Unit, cargando: Boolean) {
    if (cancha == null) { Pagina { Text("La instalación ya no está disponible.") }; return }
    val estado = estadoBloque(cancha, f, datos)
    val lista = datos.esperas.filter { it.canchaId == cancha.id && it.fecha == f.fecha && it.horaInicio == f.horaInicio }
    val posicion = lista.indexOfFirst { it.usuarioId == usuarioId }
    Pagina {
        Text(cancha.nombre, style = MaterialTheme.typography.headlineSmall)
        EtiquetaEstado(estado)
        IconoInstalacion(cancha.tipo)
        Text(cancha.tipo)
        Text("Capacidad máxima ${cancha.capacidad} personas")
        ResumenHorarios(cancha)
        if (cancha.observaciones.isNotBlank()) Text(cancha.observaciones)
        TituloSeccion("Bloque seleccionado")
        Text("${Fechas.etiqueta(f.fecha)} · ${Fechas.horario(f.horaInicio)}")
        if (estado == "Disponible") BotonPersonalizado("Solicitar este horario", reservar, habilitado = !cargando)
        if (estado == "Ocupada" || posicion >= 0) {
            TituloSeccion("Lista de espera")
            Text("${lista.size} personas en espera. Si se libera el bloque, se revisan las cuentas por orden de llegada. La asignación depende de tus tokens y de las reglas vigentes.")
            if (posicion >= 0) Text("Estás en la posición ${posicion + 1}.")
            BotonPersonalizado(if (posicion >= 0) "Salir de la lista de espera" else "Unirme a la lista de espera", espera, cargando = cargando)
        }
    }
}
