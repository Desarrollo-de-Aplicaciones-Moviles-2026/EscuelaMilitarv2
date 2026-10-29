package com.example.aplicacinejercito.model

import java.time.LocalDate

enum class PeriodoReporte(val etiqueta: String) {
    PROXIMOS("Próximos 30 días"), SEMANA("Últimos 7 días"), MES("Últimos 30 días"), MES_ACTUAL("Este mes"), PERSONALIZADO("Personalizado")
}
data class ReporteFiltros(
    val periodo: PeriodoReporte = PeriodoReporte.PROXIMOS,
    val desde: String = Fechas.hoy().minusDays(29).toString(),
    val hasta: String = Fechas.hoy().toString()
) {
    fun rango(hoy: LocalDate): Pair<LocalDate, LocalDate>? = when (periodo) {
        PeriodoReporte.PROXIMOS -> hoy to hoy.plusDays(29)
        PeriodoReporte.SEMANA -> hoy.minusDays(6) to hoy
        PeriodoReporte.MES -> hoy.minusDays(29) to hoy
        PeriodoReporte.MES_ACTUAL -> hoy.withDayOfMonth(1) to hoy
        PeriodoReporte.PERSONALIZADO -> {
            val inicio = Fechas.leer(desde)
            val fin = Fechas.leer(hasta)
            if (inicio == null || fin == null || fin.isBefore(inicio)) null else inicio to fin
        }
    }
    fun filtrar(reservas: List<ReservaModel>, hoy: LocalDate): List<ReservaModel> {
        val (inicio, fin) = rango(hoy) ?: return emptyList()
        return reservas.filter { r -> Fechas.leer(r.fecha)?.let { !it.isBefore(inicio) && !it.isAfter(fin) } == true }
    }
}
