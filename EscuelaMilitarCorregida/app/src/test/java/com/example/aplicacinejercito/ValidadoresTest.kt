package com.example.aplicacinejercito

import com.example.aplicacinejercito.model.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ValidadoresTest {
    private val hoy = LocalDate.of(2026, 10, 28)
    private val clock = Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), Fechas.zona)
    private val usuario = UsuarioModel("ml", "Martín Leiva", "m.leiva@escuelamilitar.cl")
    private val cancha = CanchaModel("tenis", "Tenis", "Tenis", 8)
    private val politica = PoliticaModel(Grado.CADETE, 1, 3)
    private val formulario = ReservaFormulario("tenis", fecha = "2026-10-29", aceptaNormas = true)
    private fun reserva(id: String = "r", propietario: String = "otro", estado: EstadoReserva = EstadoReserva.PENDIENTE,
                        semana: String = Fechas.semana(hoy)) = ReservaModel(id, propietario, "tenis", "2026-10-29", 9, estado, semana)
    private fun error(f: ReservaFormulario = formulario, reservas: List<ReservaModel> = emptyList(), c: CanchaModel = cancha,
                      eventos: List<EventoModel> = emptyList(), relaciones: List<EventoCanchaModel> = emptyList()) =
        ReglasReserva.errorSolicitud(usuario, c, politica, f, reservas, eventos, relaciones, clock)

    @Test fun correoNormalizadoEsValido() { assertNull(ValidadorLogin.validar(" M.LEIVA@ESCUELAMILITAR.CL ", "Clave123").correo) }
    @Test fun rechazaDominioFalsoYCorreoVacio() {
        assertNotNull(ValidadorLogin.validar("@escuelamilitar.cl", "Clave123").correo)
        assertNotNull(ValidadorLogin.validar("u@escuelamilitar.cl.evil", "Clave123").correo)
        assertNotNull(ValidadorLogin.validar("u@@escuelamilitar.cl", "Clave123").correo)
    }
    @Test fun validaReglaHeredadaDeClaveSinExcepcionPorVacio() {
        listOf("", "clave123", "Clave", "Clave1234").forEach { assertNotNull(ValidadorLogin.validar(usuario.correo, it).clave) }
        assertNull(ValidadorLogin.validar(usuario.correo, "Clave123").clave)
    }
    @Test fun reservaValida() { assertNull(error()) }
    @Test fun limiteAnticipacionEsInclusivo() {
        assertNull(error(formulario.copy(fecha = "2026-10-31")))
        assertNotNull(error(formulario.copy(fecha = "2026-11-01")))
    }
    @Test fun fechasInvalidasYPasadasNoSeAceptan() {
        listOf("2026-02-30", "28/10/2026", "2026-10-27", "").forEach { assertNotNull(error(formulario.copy(fecha = it))) }
    }
    @Test fun noPermiteBloqueQueYaComenzo() { assertNotNull(error(formulario.copy(fecha = hoy.toString(), horaInicio = 8))) }
    @Test fun rechazaSinNormasYFueraDelHorario() {
        assertNotNull(error(formulario.copy(aceptaNormas = false)))
        assertNotNull(error(formulario.copy(horaInicio = 7)))
        assertNotNull(error(formulario.copy(horaInicio = 18)))
        assertNotNull(error(c = cancha.copy(estado = EstadoCancha.MANTENIMIENTO)))
    }
    @Test fun solicitudPendienteOcupaBloqueYCancelacionLoLibera() {
        assertNotNull(error(reservas = listOf(reserva())))
        assertNull(error(reservas = listOf(reserva(estado = EstadoReserva.CANCELADA))))
        assertNull(error(reservas = listOf(reserva(estado = EstadoReserva.RECHAZADA))))
    }
    @Test fun usuarioNoPuedeReservarDosInstalacionesAlMismoTiempo() {
        assertNotNull(error(reservas = listOf(reserva(propietario = "ml").copy(canchaId = "pista"))))
    }
    @Test fun tokenSeCuentaEnSemanaDeSolicitudYSeRenuevaEnLunes() {
        assertEquals("2026-10-26", Fechas.semana(hoy))
        assertEquals("2026-10-26", Fechas.semana(LocalDate.of(2026, 11, 1)))
        assertEquals("2026-11-02", Fechas.semana(LocalDate.of(2026, 11, 2)))
        val r = reserva(propietario = "ml")
        assertEquals(1, ReglasReserva.tokensUsados(listOf(r), "ml", hoy))
        assertEquals(0, ReglasReserva.tokensUsados(listOf(r), "ml", hoy.plusDays(7)))
        assertNotNull(error(reservas = listOf(r.copy(horaInicio = 12))))
        assertNull(error(reservas = listOf(r.copy(horaInicio = 12, semanaSolicitud = "2026-10-19"))))
    }
    @Test fun eventoBloqueaIntervaloPeroPermiteBloqueAdyacente() {
        val e = EventoModel("e", "Torneo", "Tenis", "2026-10-29", 9, 11, 8, 1)
        val rel = listOf(EventoCanchaModel("e", "tenis"))
        assertNotNull(error(eventos = listOf(e), relaciones = rel))
        assertNull(error(formulario.copy(horaInicio = 11), eventos = listOf(e), relaciones = rel))
        assertNull(error(eventos = listOf(e.copy(activo = false)), relaciones = rel))
    }
    @Test fun validadoresDeInstalacionYEventoControlanNumerosYFechas() {
        assertNotNull(ValidadorCancha.validar(CanchaFormulario(nombre = "Tenis", capacidad = "x")).capacidad)
        assertNotNull(ValidadorCancha.validar(CanchaFormulario(nombre = "Tenis", apertura = "20", cierre = "8")).horario)
        assertNotNull(ValidadorEvento.validar(EventoFormulario(nombre = "Copa", fecha = "2026-02-30"), hoy).fecha)
        assertNotNull(ValidadorEvento.validar(EventoFormulario(nombre = "Copa", fecha = "2026-10-29", minimo = "9"), hoy).equipos)
    }

    @Test fun horarioPredeterminadoTieneDiezBloquesYFinExclusivo() {
        assertEquals((8..17).toSet(), Horarios.horas(cancha.bloquesDisponibles))
        assertTrue(cancha.permite("2026-10-29", 17))
        assertFalse(cancha.permite("2026-10-29", 18))
    }
    @Test fun seRespetanDiasYSaltosEnLaJornada() {
        val c = cancha.copy(diasActivos = Horarios.mascara(setOf(0, 2)), bloquesDisponibles = Horarios.mascara(setOf(8, 10)))
        assertTrue(c.permite("2026-10-28", 10))
        assertFalse(c.permite("2026-10-28", 9))
        assertFalse(c.permite("2026-10-29", 10))
        assertNotNull(error(c = c))
    }
    @Test fun calendarioDeshabilitaPasadoYMasAllaDelLimite() {
        assertFalse(Horarios.fechaPermitida(hoy.minusDays(1), hoy, hoy.plusDays(3)))
        assertTrue(Horarios.fechaPermitida(hoy, hoy, hoy.plusDays(3)))
        assertTrue(Horarios.fechaPermitida(hoy.plusDays(3), hoy, hoy.plusDays(3)))
        assertFalse(Horarios.fechaPermitida(hoy.plusDays(4), hoy, hoy.plusDays(3)))
    }
    @Test fun reportesIncluyenAmbosExtremosDelPeriodoYExcluyenFuera() {
        val filtro = ReporteFiltros(PeriodoReporte.PERSONALIZADO, "2026-10-29", "2026-10-31")
        val lista = listOf(reserva().copy(fecha = "2026-10-28"), reserva().copy(fecha = "2026-10-29"), reserva().copy(fecha = "2026-10-31"), reserva().copy(fecha = "2026-11-01"))
        assertEquals(listOf("2026-10-29", "2026-10-31"), filtro.filtrar(lista, hoy).map { it.fecha })
        assertNull(filtro.copy(hasta = "2026-10-28").rango(hoy))
        assertEquals(hoy.minusDays(6) to hoy, ReporteFiltros(PeriodoReporte.SEMANA).rango(hoy))
    }
    @Test fun notificacionesSonIndependientesPorCuentaYConservanTipos() {
        val preferencias = PreferenciasModel(notificacionesPorUsuario = mapOf("ml" to false), tiposSilenciados = mapOf("ml" to setOf(TipoNotificacion.EVENTOS)))
        assertFalse(preferencias.permite("ml", TipoNotificacion.RESERVAS))
        assertTrue(preferencias.permite("admin", TipoNotificacion.EVENTOS))
        val activas = preferencias.copy(notificacionesPorUsuario = mapOf("ml" to true))
        assertTrue(activas.permite("ml", TipoNotificacion.RESERVAS))
        assertFalse(activas.permite("ml", TipoNotificacion.EVENTOS))
    }
    @Test fun validacionDeHorarioRechazaDiasVaciosYBloquesFueraDeJornada() {
        assertNotNull(ValidadorCancha.validar(CanchaFormulario(nombre = "Tenis", dias = emptySet())).horario)
        assertNotNull(ValidadorCancha.validar(CanchaFormulario(nombre = "Tenis", bloques = setOf(20))).horario)
    }
}
