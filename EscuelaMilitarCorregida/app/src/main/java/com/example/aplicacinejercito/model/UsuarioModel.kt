package com.example.aplicacinejercito.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Rol { USUARIO, ADMIN }
enum class Grado(val etiqueta: String) { TENIENTE("Teniente"), CABO("Cabo"), CADETE("Cadete") }

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioModel(
    @PrimaryKey val id: String,
    val nombre: String,
    val correo: String,
    val grado: Grado = Grado.CADETE,
    val compania: String = "Bravo",
    val rol: Rol = Rol.USUARIO,
    val activo: Boolean = true
)

@Entity(tableName = "politicas")
data class PoliticaModel(
    @PrimaryKey val grado: Grado,
    val tokensSemana: Int,
    val anticipacionDias: Int,
    val requiereAprobacion: Boolean = true
)
