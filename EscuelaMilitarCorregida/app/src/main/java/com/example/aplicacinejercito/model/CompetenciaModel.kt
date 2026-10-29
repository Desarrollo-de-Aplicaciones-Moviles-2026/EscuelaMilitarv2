package com.example.aplicacinejercito.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class EventoModel(
    @PrimaryKey val id: String,
    val nombre: String,
    val disciplina: String,
    val fecha: String,
    val horaInicio: Int,
    val horaFin: Int,
    val cupoEquipos: Int,
    val minimoIntegrantes: Int,
    val descripcion: String = "",
    val activo: Boolean = true
)

@Entity(tableName = "evento_canchas", primaryKeys = ["eventoId", "canchaId"])
data class EventoCanchaModel(val eventoId: String, val canchaId: String)

@Entity(tableName = "equipos", indices = [Index(value = ["eventoId", "capitanId"], unique = true)])
data class EquipoModel(
    @PrimaryKey val id: String,
    val eventoId: String,
    val capitanId: String,
    val nombre: String,
    val compania: String
)

@Entity(tableName = "equipo_integrantes", primaryKeys = ["equipoId", "usuarioId"])
data class IntegranteModel(val equipoId: String, val usuarioId: String)

@Entity(tableName = "espera", indices = [Index(value = ["usuarioId", "canchaId", "fecha", "horaInicio"], unique = true)])
data class EsperaModel(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val canchaId: String,
    val fecha: String,
    val horaInicio: Int,
    val creadaEn: Long
)

enum class TipoNotificacion(val etiqueta: String) {
    SOLICITUDES("Solicitudes"), RESERVAS("Reservas"), LISTA_ESPERA("Lista de espera"),
    EVENTOS("Eventos y competencias"), CUENTA("Cuenta")
}

@Entity(tableName = "notificaciones")
data class NotificacionModel(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val titulo: String,
    val detalle: String,
    val creadoEn: Long,
    val tipo: TipoNotificacion
)
