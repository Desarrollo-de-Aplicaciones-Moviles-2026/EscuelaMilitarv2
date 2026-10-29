package com.example.aplicacinejercito.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoCancha(val etiqueta: String) {
    HABILITADA("Disponible"), MANTENIMIENTO("Mantenimiento"), NO_DISPONIBLE("No disponible")
}

@Entity(tableName = "canchas")
data class CanchaModel(
    @PrimaryKey val id: String,
    val nombre: String,
    val tipo: String,
    val capacidad: Int,
    val estado: EstadoCancha = EstadoCancha.HABILITADA,
    val horaApertura: Int = 8,
    val horaCierre: Int = 18,
    val observaciones: String = "",
    val activa: Boolean = true,
    @ColumnInfo(defaultValue = "127") val diasActivos: Int = 127,
    @ColumnInfo(defaultValue = "261888") val bloquesDisponibles: Int = 261888
) {
    fun permite(fecha: String, hora: Int): Boolean {
        val dia = Fechas.leer(fecha)?.dayOfWeek?.value ?: return false
        return hora in horaApertura until horaCierre && Horarios.contiene(diasActivos, dia - 1) && Horarios.contiene(bloquesDisponibles, hora)
    }
}
