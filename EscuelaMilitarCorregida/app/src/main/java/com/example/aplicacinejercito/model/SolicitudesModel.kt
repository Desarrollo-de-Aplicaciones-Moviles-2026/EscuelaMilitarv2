package com.example.aplicacinejercito.model

// Reglas de filtrado de la bandeja de solicitudes. Sin estado de UI ni dependencias de Android.
// clave: valor de SolicitudesUiState.grado; etiqueta: texto del chip en plural.
data class ConteoGrado(val clave: String, val etiqueta: String, val cantidad: Int)

object SolicitudesModel {
    const val TODOS = "Todos"

    fun pendiente(reserva: ReservaModel): Boolean = reserva.estado == EstadoReserva.PENDIENTE

    fun contarPendientes(datos: DatosApp): Int = datos.reservas.count(::pendiente)

    fun contarProcesadas(datos: DatosApp): Int = datos.reservas.count { !pendiente(it) }

    fun filtrar(datos: DatosApp, filtros: SolicitudesUiState): List<ReservaModel> =
        datos.reservas
            .filter { if (filtros.procesadas) !pendiente(it) else pendiente(it) }
            .filter { filtros.grado == TODOS || gradoDe(datos, it)?.etiqueta == filtros.grado }
            .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))

    // Conteo por grado sobre la pestaña activa, para mostrar en los filtros.
    fun contarPorGrado(datos: DatosApp, procesadas: Boolean): List<ConteoGrado> {
        val base = datos.reservas.filter { if (procesadas) !pendiente(it) else pendiente(it) }
        return listOf(ConteoGrado(TODOS, "Todos", base.size)) + Grado.entries.map { grado ->
            ConteoGrado(grado.etiqueta, grado.etiqueta + "s", base.count { gradoDe(datos, it) == grado })
        }
    }

    fun gradoDe(datos: DatosApp, reserva: ReservaModel): Grado? =
        datos.usuarios.firstOrNull { it.id == reserva.usuarioId }?.grado

    const val LIMITE_MOTIVO = 200

    // El motivo "Otro" es obligatorio y tiene límite de caracteres; las demás opciones no requieren texto.
    fun errorRechazo(f: RechazoFormulario): String? {
        if (f.motivo != MotivoRechazo.OTRO) return null
        val texto = f.otro.trim()
        return when {
            texto.isEmpty() -> "Escribe el motivo del rechazo."
            texto.length > LIMITE_MOTIVO -> "El motivo no puede superar los $LIMITE_MOTIVO caracteres."
            else -> null
        }
    }

    fun textoMotivo(f: RechazoFormulario): String =
        if (f.motivo == MotivoRechazo.OTRO) f.otro.trim() else f.motivo.etiqueta

    // Solo las pendientes pueden aprobarse o rechazarse en lote.
    fun seleccionables(reservas: List<ReservaModel>): Set<String> =
        reservas.filter(::pendiente).map { it.id }.toSet()
}
