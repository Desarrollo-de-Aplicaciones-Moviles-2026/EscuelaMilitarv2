package com.example.aplicacinejercito.model

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object Fechas {
    val zona: ZoneId = ZoneId.of("America/Santiago")
    fun hoy(clock: Clock = Clock.system(zona)): LocalDate = LocalDate.now(clock)
    fun semana(fecha: LocalDate): String = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
    fun leer(texto: String): LocalDate? = runCatching { LocalDate.parse(texto) }.getOrNull()
    fun etiqueta(fecha: String): String = leer(fecha)?.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-CL"))) ?: fecha
    fun horario(hora: Int): String = "%02d:00 – %02d:00".format(hora, hora + 1)
    fun futuro(fecha: String, hora: Int, clock: Clock = Clock.system(zona)): Boolean =
        leer(fecha)?.atTime(hora.coerceIn(0, 23), 0)?.isAfter(LocalDateTime.now(clock)) == true
}

object ValidadorLogin {
    // Regla heredada del proyecto original. No es una exigencia explícita de los PPT.
    fun validar(correo: String, clave: String): LoginErrores {
        val correoNormalizado = correo.trim().lowercase(Locale.ROOT)
        val errorCorreo = when {
            correoNormalizado.isBlank() -> "Ingresa tu correo institucional."
            !Regex("^[a-z0-9._%+-]+@escuelamilitar\\.cl$").matches(correoNormalizado) -> "Usa un correo válido @escuelamilitar.cl."
            else -> null
        }
        val errorClave = when {
            clave.isBlank() -> "Ingresa tu contraseña."
            !clave.first().isUpperCase() -> "Debe comenzar con una letra mayúscula."
            !clave.any(Char::isDigit) -> "Debe incluir al menos un número."
            clave.length > 8 -> "No debe superar los 8 caracteres."
            else -> null
        }
        return LoginErrores(errorCorreo, errorClave)
    }
}

object ReglasReserva {
    fun validarFormulario(f: ReservaFormulario, hoy: LocalDate, anticipacion: Int, exigirNormas: Boolean = true): ReservaErrores {
        val fecha = Fechas.leer(f.fecha)
        return ReservaErrores(
            fecha = when {
                fecha == null -> "Ingresa una fecha válida con formato AAAA-MM-DD."
                fecha.isBefore(hoy) -> "La fecha no puede estar en el pasado."
                fecha.isAfter(hoy.plusDays(anticipacion.toLong())) -> "Tu grado permite reservar hasta $anticipacion días antes."
                else -> null
            },
            hora = if (f.horaInicio !in 0..23) "Selecciona una hora válida." else null,
            normas = if (exigirNormas && !f.aceptaNormas) "Debes aceptar las normas de uso." else null
        )
    }

    fun tokensUsados(reservas: List<ReservaModel>, usuarioId: String, hoy: LocalDate): Int = reservas.count {
        it.usuarioId == usuarioId && it.semanaSolicitud == Fechas.semana(hoy) && it.ocupaHorario
    }

    fun errorSolicitud(
        usuario: UsuarioModel, cancha: CanchaModel, politica: PoliticaModel,
        f: ReservaFormulario, reservas: List<ReservaModel>, eventos: List<EventoModel>,
        relaciones: List<EventoCanchaModel>, clock: Clock = Clock.system(Fechas.zona)
    ): String? {
        val errores = validarFormulario(f, Fechas.hoy(clock), politica.anticipacionDias)
        if (errores != ReservaErrores()) return errores.fecha ?: errores.hora ?: errores.normas
        if (!usuario.activo || usuario.rol != Rol.USUARIO) return "La cuenta no está habilitada para reservar."
        if (!cancha.activa || cancha.estado != EstadoCancha.HABILITADA) return "La instalación no está disponible."
        if (!Fechas.futuro(f.fecha, f.horaInicio, clock)) return "El bloque ya comenzó. Selecciona otro horario."
        if (!cancha.permite(f.fecha, f.horaInicio)) return "La instalación está cerrada en este día o bloque."
        if (tokensUsados(reservas, usuario.id, Fechas.hoy(clock)) >= politica.tokensSemana) return "No te quedan tokens esta semana."
        if (reservas.any { it.ocupaHorario && it.usuarioId == usuario.id && it.fecha == f.fecha && it.horaInicio == f.horaInicio }) return "Ya tienes una reserva o solicitud en este horario."
        if (reservas.any { it.ocupaHorario && it.canchaId == cancha.id && it.fecha == f.fecha && it.horaInicio == f.horaInicio }) return "El horario acaba de ser reservado. Elige otro bloque."
        if (bloqueadoPorEvento(cancha.id, f.fecha, f.horaInicio, eventos, relaciones)) return "Horario reservado para un evento institucional."
        return null
    }

    fun bloqueadoPorEvento(canchaId: String, fecha: String, hora: Int, eventos: List<EventoModel>, relaciones: List<EventoCanchaModel>): Boolean = eventos.any { evento ->
        evento.activo && evento.fecha == fecha && hora >= evento.horaInicio && hora < evento.horaFin &&
            relaciones.any { it.eventoId == evento.id && it.canchaId == canchaId }
    }
}

object ValidadorCancha {
    fun validar(f: CanchaFormulario): CanchaErrores {
        val apertura = f.apertura.toIntOrNull()
        val cierre = f.cierre.toIntOrNull()
        return CanchaErrores(
            nombre = if (f.nombre.trim().length < 3) "Ingresa un nombre de al menos 3 caracteres." else null,
            capacidad = if ((f.capacidad.toIntOrNull() ?: 0) !in 1..10000) "La capacidad debe estar entre 1 y 10000." else null,
            horario = if (apertura == null || cierre == null || apertura !in 0..23 || cierre !in 1..24 || cierre <= apertura || f.dias.isEmpty() || f.dias.any { it !in 1..7 } || f.bloques.isEmpty() || f.bloques.any { it !in apertura until cierre }) "Selecciona días y bloques válidos dentro del horario de apertura." else null
        )
    }
}

object ValidadorEvento {
    fun validar(f: EventoFormulario, hoy: LocalDate): EventoErrores {
        val fecha = Fechas.leer(f.fecha)
        val inicio = f.inicio.toIntOrNull()
        val fin = f.fin.toIntOrNull()
        return EventoErrores(
            nombre = if (f.nombre.trim().length < 3) "Ingresa un nombre de al menos 3 caracteres." else null,
            fecha = if (fecha == null || fecha.isBefore(hoy)) "Ingresa una fecha válida desde hoy, en formato AAAA-MM-DD." else null,
            horario = if (inicio == null || fin == null || inicio !in 0..23 || fin !in 1..24 || fin <= inicio) "El inicio y fin deben formar un intervalo válido entre 0 y 24." else null,
            equipos = if ((f.cupos.toIntOrNull() ?: 0) !in 1..100 || (f.minimo.toIntOrNull() ?: 0) !in 1..8) "Usa entre 1 y 100 equipos y entre 1 y 8 integrantes por equipo." else null,
            instalaciones = if (f.canchas.isEmpty()) "Selecciona al menos una instalación." else null
        )
    }
}

object ValidadorEquipo {
    fun validar(f: EquipoFormulario): EquipoErrores = EquipoErrores(
        nombre = if (f.nombre.trim().length < 3) "Ingresa un nombre de al menos 3 caracteres." else null,
        integrantes = if (f.integrantes.size < f.minimoIntegrantes) "Selecciona al menos ${f.minimoIntegrantes} integrantes, incluido el capitán." else null
    )
}
