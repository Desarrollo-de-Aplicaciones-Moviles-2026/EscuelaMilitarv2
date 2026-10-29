package com.example.aplicacinejercito.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun NuevaReservaScreen(f: ReservaFormulario, datos: DatosApp, politica: PoliticaModel,
                       cambiar: ((ReservaFormulario) -> ReservaFormulario) -> Unit, buscar: () -> Unit) {
    val hoy = Fechas.hoy()
    val limite = hoy.plusDays(politica.anticipacionDias.toLong())
    val canchas = datos.canchas.filter { it.activa && it.estado == EstadoCancha.HABILITADA && (f.tipo == "Todos" || it.tipo == f.tipo) }
    val horas = ((8..17).toList() + canchas.flatMap { Horarios.horas(it.bloquesDisponibles) }).distinct().sorted()
    val fechaValida = Fechas.leer(f.fecha)?.let { Horarios.fechaPermitida(it, hoy, limite) } == true
    val elegibles = horas.filter { hora -> fechaValida && Fechas.futuro(f.fecha, hora) && canchas.any { it.permite(f.fecha, hora) } }
    LaunchedEffect(f.fecha, f.tipo, elegibles) {
        if (f.horaInicio !in elegibles && elegibles.isNotEmpty()) cambiar { it.copy(horaInicio = elegibles.first()) }
    }
    Pagina {
        Text("Paso 1 de 3 · Fecha y horario", style = MaterialTheme.typography.labelLarge)
        Selector("Tipo de instalación", f.tipo, listOf("Todos") + datos.canchas.filter { it.activa }.map { it.tipo }.distinct().sorted(), { valor -> cambiar { it.copy(tipo = valor) } })
        CampoFecha(f.fecha, { valor -> cambiar { it.copy(fecha = valor) } }, f.errores.fecha, minimo = hoy, maximo = limite)
        Text("Puedes reservar desde hoy hasta ${Fechas.etiqueta(limite.toString())} (${politica.anticipacionDias} días de anticipación).", style = MaterialTheme.typography.bodySmall)
        TituloSeccion("Selecciona un bloque de una hora")
        BloquesHorarios(horas, setOf(f.horaInicio), { hora -> cambiar { it.copy(horaInicio = hora) } }, habilitado = { it in elegibles })
        if (elegibles.isEmpty()) Text("No hay bloques habilitados para esta fecha y tipo de instalación. Prueba otra fecha o tipo.", color = MaterialTheme.colorScheme.error)
        Text("Los bloques grises ya comenzaron o están fuera del horario configurado. La disponibilidad de cada instalación se muestra en el siguiente paso.", style = MaterialTheme.typography.bodySmall)
        MensajeError(f.errores.hora)
        BotonPersonalizado("Ver instalaciones", buscar, habilitado = f.horaInicio in elegibles)
    }
}
