package com.example.aplicacinejercito.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.Ignore
import androidx.room.PrimaryKey

enum class EstadoReserva(val etiqueta: String) {
    PENDIENTE("Pendiente"), CONFIRMADA("Confirmada"), RECHAZADA("Rechazada"), CANCELADA("Cancelada")
}

@Entity(tableName = "reservas", indices = [Index(value = ["canchaId", "fecha", "horaInicio"])])
data class ReservaModel(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val canchaId: String,
    val fecha: String,
    val horaInicio: Int,
    val estado: EstadoReserva,
    val semanaSolicitud: String
) {
    @get:Ignore
    val ocupaHorario: Boolean get() = estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
}
