package com.example.aplicacinejercito

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.aplicacinejercito.data.*
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.repository.*
import java.time.Clock
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class PersistenciaTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), Fechas.zona)
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun f() = ReservaFormulario("tenis", fecha = "2026-10-29", horaInicio = 11, aceptaNormas = true)
    private inline fun <R> AppDatabase.usar(bloque: (AppDatabase) -> R): R = try { bloque(this) } finally { close() }
    private fun nueva() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    private fun abrirVersionUno(nombre: String): SupportSQLiteOpenHelper {
        val esquema = JSONObject(javaClass.getResourceAsStream("/esquema_v1.json")!!.bufferedReader().use { it.readText() }).getJSONObject("database")
        return FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context).name(nombre)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val entidades = esquema.getJSONArray("entities")
                    for (i in 0 until entidades.length()) {
                        val entidad = entidades.getJSONObject(i); val tabla = entidad.getString("tableName")
                        db.execSQL(entidad.getString("createSql").replace("\${TABLE_NAME}", tabla))
                        val indices = entidad.getJSONArray("indices")
                        for (j in 0 until indices.length()) db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", tabla))
                    }
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
    }
    @Test fun migrarBaseAntiguaVaciaPermiteInicializarElCatalogoCompleto() = runBlocking {
        val nombre = "migracion-vacia-${UUID.randomUUID()}.db"
        abrirVersionUno(nombre).let { it.writableDatabase; it.close() }
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(Migraciones.MIGRACION_1_2).build().usar { db ->
                val repo = DeportesRepository(db, clock)
                repo.inicializar()
                val datos = repo.observarDatos().first()
                assertEquals(20, datos.canchas.size)
                assertEquals(34, datos.usuarios.size)
            }
        } finally { context.deleteDatabase(nombre) }
    }
    @Test fun catalogoInicialTieneVeinteEspaciosYSeisCanchasDeTenis() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val datos = repo.observarDatos().first()
            assertEquals(20, datos.canchas.count { it.activa })
            assertEquals(6, datos.canchas.count { it.tipo == "Cancha de tenis" })
            assertTrue(datos.canchas.all { it.horaApertura == 8 && it.horaCierre == 18 && Horarios.horas(it.bloquesDisponibles).size == 10 })
            assertTrue(datos.eventos.all { it.horaFin <= 18 })
        }
    }
    @Test fun dosSolicitudesConcurrentesNoDuplicanBloque() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val resultados = listOf("ml", "cp").map { id -> async { runCatching { repo.solicitarReserva(id, f()) } } }.awaitAll()
            assertEquals(1, resultados.count { it.isSuccess })
            assertEquals(1, db.dao().reservas().count { it.canchaId == "tenis" && it.horaInicio == 11 && it.ocupaHorario })
        }
    }
    @Test fun pendienteNoSeCancelaYSeRechazaSinMotivoLiberandoElToken() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val id = repo.solicitarReserva("ml", f())
            assertTrue(runCatching { repo.cancelarReserva("admin", id) }.isFailure)
            assertTrue(runCatching { repo.cancelarReserva("ml", id) }.isFailure)
            assertTrue(runCatching { repo.resolverSolicitud("ml", id, true) }.isFailure)
            repo.resolverSolicitud("admin", id, false)
            assertEquals(EstadoReserva.RECHAZADA, db.dao().reserva(id)?.estado)
            assertEquals(0, ReglasReserva.tokensUsados(db.dao().reservas(), "ml", Fechas.hoy(clock)))
        }
    }
    @Test fun cancelarConfirmadaPromueveListaDeEsperaEnOrden() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val id = repo.solicitarReserva("ml", f())
            repo.alternarEspera("cp", f()); repo.alternarEspera("vs", f())
            repo.resolverSolicitud("admin", id, true); repo.cancelarReserva("ml", id)
            assertEquals(EstadoReserva.CANCELADA, db.dao().reserva(id)?.estado)
            assertTrue(db.dao().reservas().any { it.usuarioId == "cp" && it.canchaId == "tenis" && it.horaInicio == 11 && it.estado == EstadoReserva.PENDIENTE })
            assertEquals("vs", db.dao().espera("tenis", "2026-10-29", 11).single().usuarioId)
        }
    }
    @Test fun horariosPersistenYNoPuedenInvalidarCompromisosFuturos() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val f = CanchaFormulario(id = "tenis", nombre = "Tenis", tipo = "Cancha de tenis", capacidad = "8", dias = setOf(4), bloques = setOf(11, 13))
            repo.guardarCancha("admin", f)
            assertEquals(setOf(11, 13), Horarios.horas(db.dao().cancha("tenis")!!.bloquesDisponibles))
            assertTrue(runCatching { repo.solicitarReserva("ml", this@PersistenciaTest.f().copy(horaInicio = 12)) }.isFailure)
            repo.solicitarReserva("ml", this@PersistenciaTest.f())
            assertTrue(runCatching { repo.guardarCancha("admin", f.copy(bloques = setOf(13))) }.isFailure)
        }
    }
    @Test fun cadaOperacionGeneraSuTipoDeNotificacion() = runBlocking {
        nueva().usar { db ->
            val repo = DeportesRepository(db, clock); repo.inicializar()
            val id = repo.solicitarReserva("ml", f()); repo.resolverSolicitud("admin", id, true); repo.cancelarReserva("admin", id)
            val propias = repo.observarDatos().first().notificaciones.filter { it.usuarioId == "ml" }
            assertTrue(propias.any { it.tipo == TipoNotificacion.SOLICITUDES })
            assertTrue(propias.any { it.tipo == TipoNotificacion.RESERVAS })
        }
    }
    @Test fun preferenciasGeneralesYPorTipoPersistenSeparadasEntreCuentas() = runBlocking {
        val id = UUID.randomUUID().toString()
        val repo = PreferenciasRepository(context)
        repo.cambiarNotificaciones(id, false); repo.cambiarTipo(id, TipoNotificacion.EVENTOS, false)
        val prefs = PreferenciasRepository(context).preferencias.first()
        assertFalse(prefs.generales(id))
        assertFalse(prefs.tipoActivo(id, TipoNotificacion.EVENTOS))
        assertTrue(prefs.generales("otra-$id"))
        repo.cambiarNotificaciones(id, true)
        assertFalse(repo.preferencias.first().permite(id, TipoNotificacion.EVENTOS))
    }
    @Test fun migracionUnoADosConservaReservasYEliminaCamposRetirados() = runBlocking {
        val nombre = "migracion-${UUID.randomUUID()}.db"
        val helper = abrirVersionUno(nombre)
        val antigua = helper.writableDatabase
        antigua.execSQL("INSERT INTO usuarios VALUES ('ml','Martín Leiva','m.leiva@escuelamilitar.cl','CADETE','Bravo','USUARIO',1)")
        listOf("pista", "mc1", "voley", "basquet", "rugby1", "rugby2", "muscu", "estadio", "baby", "piscina", "mc2", "futbol1", "combate", "escalada").forEach { id ->
            antigua.execSQL("INSERT INTO canchas VALUES (?,?,'Instalación',30,'HABILITADA',8,20,'',1)", arrayOf(id, id))
        }
        antigua.execSQL("INSERT INTO canchas VALUES ('tenis','Canchas de tenis','Cancha de tenis',8,'HABILITADA',8,20,'',1)")
        antigua.execSQL("INSERT INTO reservas VALUES ('r','ml','tenis','2099-10-29',19,4,'motivo antiguo','CONFIRMADA','2099-10-26','respuesta antigua')")
        antigua.execSQL("INSERT INTO espera VALUES ('e','cp','tenis','2099-10-29',19,4,1)")
        antigua.execSQL("INSERT INTO avisos VALUES ('n','ml','Solicitud rechazada','motivo antiguo',1)")
        helper.close()
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(Migraciones.MIGRACION_1_2).build().usar { db ->
                val espacios = db.dao().observarCanchas().first()
                assertEquals(20, espacios.count { it.activa })
                assertEquals(6, espacios.count { it.tipo == "Cancha de tenis" })
                assertTrue(espacios.filter { it.id in listOf("rugby1", "piscina", "escalada") }.all { !it.activa })
                val r = db.dao().reserva("r")!!
                assertEquals("2099-10-29", r.fecha); assertEquals(19, r.horaInicio); assertEquals(EstadoReserva.CONFIRMADA, r.estado)
                assertEquals("cp", db.dao().espera("tenis", "2099-10-29", 19).single().usuarioId)
                assertTrue(db.dao().cancha("tenis")!!.permite("2099-10-29", 19))
                val notificacion = db.dao().observarNotificaciones().first().single()
                assertEquals(TipoNotificacion.SOLICITUDES, notificacion.tipo)
                assertFalse(notificacion.detalle.contains("motivo antiguo"))
                db.openHelper.readableDatabase.query("PRAGMA table_info(reservas)").use { cursor ->
                    val columnas = buildList { while (cursor.moveToNext()) add(cursor.getString(1)) }
                    assertFalse(columnas.any { it in listOf("participantes", "motivo", "respuesta") })
                }
            }
        } finally { context.deleteDatabase(nombre) }
    }
}
