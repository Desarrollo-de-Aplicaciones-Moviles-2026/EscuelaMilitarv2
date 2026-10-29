package com.example.aplicacinejercito.model

/** Máscaras simples de bits para persistir selecciones de días y bloques en Room. */
object Horarios {
    fun mascara(valores: Set<Int>): Int = valores.fold(0) { resultado, valor -> resultado or (1 shl valor) }
    fun contiene(mascara: Int, valor: Int): Boolean = valor in 0..30 && mascara and (1 shl valor) != 0
    fun horas(mascara: Int): Set<Int> = (0..23).filter { contiene(mascara, it) }.toSet()
    fun dias(mascara: Int): Set<Int> = (1..7).filter { contiene(mascara, it - 1) }.toSet()
    fun fechaPermitida(fecha: java.time.LocalDate, minimo: java.time.LocalDate?, maximo: java.time.LocalDate?): Boolean =
        (minimo == null || !fecha.isBefore(minimo)) && (maximo == null || !fecha.isAfter(maximo))
}
