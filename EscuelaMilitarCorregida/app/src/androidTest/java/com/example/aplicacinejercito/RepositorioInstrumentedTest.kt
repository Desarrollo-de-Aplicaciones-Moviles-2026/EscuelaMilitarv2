package com.example.aplicacinejercito

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aplicacinejercito.data.AppDatabase
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.repository.DeportesRepository
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorioInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: DeportesRepository
    private var nombrePrueba: String? = null
    @Before fun preparar() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = DeportesRepository(db, Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), Fechas.zona))
    }
    @After fun cerrar() {
        db.close()
        nombrePrueba?.let { InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(it) }
    }
    private fun formulario() = ReservaFormulario("tenis", fecha = "2026-10-29", horaInicio = 11, aceptaNormas = true)

    @Test fun dosSolicitudesSimultaneasNoDuplicanBloque() = runTest {
        repository.inicializar()
        val resultados = listOf("ml", "cp").map { id -> async { runCatching { repository.solicitarReserva(id, formulario()) } } }.awaitAll()
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals(1, db.dao().reservas().count { it.canchaId == "tenis" && it.horaInicio == 11 && it.ocupaHorario })
    }
    @Test fun cancelarAsignaEsperaYPreservaPoliticaDeAprobacion() = runTest {
        repository.inicializar()
        val id = repository.solicitarReserva("ml", formulario())
        repository.alternarEspera("cp", formulario())
        repository.alternarEspera("vs", formulario())
        repository.resolverSolicitud("admin", id, true)
        repository.cancelarReserva("ml", id)
        val reservas = db.dao().reservas()
        assertEquals(EstadoReserva.CANCELADA, reservas.first { it.id == id }.estado)
        assertTrue(reservas.any { it.usuarioId == "cp" && it.canchaId == "tenis" && it.horaInicio == 11 && it.estado == EstadoReserva.PENDIENTE })
        assertEquals("vs", db.dao().espera("tenis", "2026-10-29", 11).single().usuarioId)
    }
    @Test fun rechazarLiberaTokenYUsuarioNoPuedeAprobar() = runTest {
        repository.inicializar()
        val id = repository.solicitarReserva("ml", formulario())
        assertTrue(runCatching { repository.resolverSolicitud("ml", id, true) }.isFailure)
        repository.resolverSolicitud("admin", id, false)
        assertEquals(EstadoReserva.RECHAZADA, db.dao().reserva(id)?.estado)
        assertEquals(0, ReglasReserva.tokensUsados(db.dao().reservas(), "ml", java.time.LocalDate.of(2026, 10, 28)))
    }
    @Test fun losDatosPersistenAlCerrarYAbrirLaBase() = runTest {
        db.close()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nombre = "prueba-${java.util.UUID.randomUUID()}.db"
        nombrePrueba = nombre
        db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        repository = DeportesRepository(db, Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), Fechas.zona))
        repository.inicializar()
        repository.solicitarReserva("ml", formulario())
        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        val otro = DeportesRepository(db)
        otro.inicializar()
        assertTrue(otro.observarDatos().first().reservas.any { it.usuarioId == "ml" })
    }
}
