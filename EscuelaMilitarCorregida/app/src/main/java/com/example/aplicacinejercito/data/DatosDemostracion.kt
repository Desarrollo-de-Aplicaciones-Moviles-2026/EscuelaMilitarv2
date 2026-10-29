package com.example.aplicacinejercito.data

import com.example.aplicacinejercito.model.*
import java.time.LocalDate

object DatosDemostracion {
    const val CLAVE = "Clave123"
    const val ADMIN_ID = "admin"
    val politicas = listOf(
        PoliticaModel(Grado.TENIENTE, 3, 14),
        PoliticaModel(Grado.CABO, 2, 7),
        PoliticaModel(Grado.CADETE, 1, 3)
    )
    val canchas = listOf(
        CanchaModel("pista", "Pista Atlética", "Pista de atletismo", 60),
        CanchaModel("muscu", "Sala de Musculación", "Sala de musculación", 25),
        CanchaModel("basquet", "Gimnasio de Básquetbol", "Gimnasio de básquetbol", 40),
        CanchaModel("voley", "Gimnasio de Vóleibol", "Gimnasio de vóleibol", 24),
        CanchaModel("estadio", "Estadio", "Estadio multiuso", 300),
        CanchaModel("rugby2", "Cancha de Rugby N.° 2", "Cancha de rugby", 45),
        CanchaModel("futbol1", "Cancha de Fútbol N.° 2", "Cancha de fútbol", 50),
        CanchaModel("tenis", "Cancha de Tenis N.° 1", "Cancha de tenis", 8),
        CanchaModel("tenis2", "Cancha de Tenis N.° 2", "Cancha de tenis", 8),
        CanchaModel("tenis3", "Cancha de Tenis N.° 3", "Cancha de tenis", 8),
        CanchaModel("tenis4", "Cancha de Tenis N.° 4", "Cancha de tenis", 8),
        CanchaModel("tenis5", "Cancha de Tenis N.° 5", "Cancha de tenis", 8),
        CanchaModel("tenis6", "Cancha de Tenis N.° 6", "Cancha de tenis", 8),
        CanchaModel("baby", "Cancha de Baby Fútbol", "Cancha de baby fútbol", 14),
        CanchaModel("futbolito", "Cancha de Futbolito", "Cancha de futbolito", 14),
        CanchaModel("mc1", "Multicancha N.° 1", "Multicancha techada", 30),
        CanchaModel("mc2", "Multicancha N.° 2", "Multicancha techada", 30),
        CanchaModel("combate", "Tatami para disciplinas de combate", "Tatami", 20),
        CanchaModel("aula", "Aula Magna", "Auditorio institucional", 300),
        CanchaModel("obstaculos", "Cancha de Obstáculos de Pentatlón Militar", "Pentatlón militar", 30)
    )
    val tipos = canchas.map { it.tipo }.distinct().sorted()

    val usuarios: List<UsuarioModel> = buildList {
        add(UsuarioModel(ADMIN_ID, "Administración", "admin@escuelamilitar.cl", rol = Rol.ADMIN, compania = "Administración"))
        add(UsuarioModel("vs", "Valentina Soto", "v.soto@escuelamilitar.cl", Grado.TENIENTE, "Alfa"))
        add(UsuarioModel("cp", "Camila Pérez", "c.perez@escuelamilitar.cl", Grado.TENIENTE, "Alfa"))
        add(UsuarioModel("dm", "Diego Morales", "d.morales@escuelamilitar.cl", Grado.CABO, "Bravo"))
        add(UsuarioModel("ml", "Martín Leiva", "m.leiva@escuelamilitar.cl", Grado.CADETE, "Bravo"))
        add(UsuarioModel("nv", "Nicolás Vidal", "n.vidal@escuelamilitar.cl", Grado.CABO, "Charlie"))
        add(UsuarioModel("tr", "Tomás Riquelme", "t.riquelme@escuelamilitar.cl", Grado.CADETE, "Charlie"))
        add(UsuarioModel("ar", "Andrés Rojas", "a.rojas@escuelamilitar.cl", Grado.CABO, "Delta"))
        val adicionales = mapOf(
            "Alfa" to listOf("Felipe Araya", "Javiera Muñoz", "Sebastián Castro", "Constanza Rojas", "Matías González", "Fernanda Díaz"),
            "Bravo" to listOf("Benjamín Silva", "Catalina Herrera", "Joaquín Flores", "Antonia Reyes", "Cristóbal Navarro", "Isidora Vargas"),
            "Charlie" to listOf("Vicente Contreras", "Francisca Torres", "Gabriel Espinoza", "Josefa Sepúlveda", "Lucas Bravo", "Trinidad Fuentes"),
            "Delta" to listOf("Maximiliano Soto", "Florencia Gutiérrez", "Agustín Pizarro", "Martina Carrasco", "Bastián Núñez", "Emilia Valenzuela", "Renato Cortés")
        )
        adicionales.forEach { (compania, nombres) -> nombres.forEachIndexed { i, nombre ->
            val id = "${compania.lowercase()}$i"
            add(UsuarioModel(id, nombre, "$id@escuelamilitar.cl", compania = compania))
        } }
        add(UsuarioModel("if", "Ignacio Fuentes", "i.fuentes@escuelamilitar.cl", compania = "Delta", activo = false))
    }

    fun eventos(hoy: LocalDate): List<EventoModel> = listOf(
        EventoModel("e1", "Copa de Fútbol Intercompañías", "Fútbol", hoy.plusDays(7).toString(), 9, 13, 8, 7, "Partidos entre compañías."),
        EventoModel("e2", "Campeonato interescuelas de atletismo", "Atletismo", hoy.plusDays(6).toString(), 9, 18, 12, 4),
        EventoModel("e3", "Copa de básquetbol cadetes", "Básquetbol", hoy.plusDays(10).toString(), 16, 18, 8, 5),
        EventoModel("e4", "Torneo de rugby seven", "Rugby", hoy.plusDays(13).toString(), 10, 14, 6, 7),
        EventoModel("e5", "Corrida aniversario 10K", "Atletismo", hoy.plusDays(14).toString(), 8, 12, 20, 1)
    )
    val eventoCanchas = listOf(
        EventoCanchaModel("e1", "futbol1"), EventoCanchaModel("e2", "estadio"), EventoCanchaModel("e2", "pista"),
        EventoCanchaModel("e3", "basquet"), EventoCanchaModel("e4", "rugby2"), EventoCanchaModel("e5", "pista")
    )
}
