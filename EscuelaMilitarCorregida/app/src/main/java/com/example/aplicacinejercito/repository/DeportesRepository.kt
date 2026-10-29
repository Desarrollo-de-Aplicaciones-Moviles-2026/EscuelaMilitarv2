package com.example.aplicacinejercito.repository

import androidx.room.withTransaction
import com.example.aplicacinejercito.data.AppDatabase
import com.example.aplicacinejercito.data.DatosDemostracion
import com.example.aplicacinejercito.model.*
import java.time.Clock
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ReglaNegocioException(message: String) : IllegalArgumentException(message)

/** Única entrada a los datos. Cada operación verifica sus reglas dentro de una transacción. */
class DeportesRepository(private val db: AppDatabase, private val clock: Clock = Clock.system(Fechas.zona)) {
    private val dao = db.dao()
    private fun id() = UUID.randomUUID().toString()
    private fun exigir(condicion: Boolean, mensaje: String) { if (!condicion) throw ReglaNegocioException(mensaje) }
    private suspend fun usuarioActivo(id: String): UsuarioModel = dao.usuario(id)?.takeIf { it.activo }
        ?: throw ReglaNegocioException("La cuenta no está activa.")
    private suspend fun administrador(id: String) {
        exigir(usuarioActivo(id).rol == Rol.ADMIN, "Esta acción requiere una cuenta administradora.")
    }
    private suspend fun notificacion(usuarioId: String, titulo: String, detalle: String, tipo: TipoNotificacion) {
        dao.insertarNotificacion(NotificacionModel(id(), usuarioId, titulo, detalle, clock.millis(), tipo))
    }

    suspend fun inicializar() = db.withTransaction {
        if (dao.cantidadUsuarios() == 0) {
            dao.insertarUsuarios(DatosDemostracion.usuarios)
            dao.insertarCanchas(DatosDemostracion.canchas)
            dao.guardarPoliticas(DatosDemostracion.politicas)
            val hoy = Fechas.hoy(clock)
            dao.insertarEventos(DatosDemostracion.eventos(hoy))
            dao.insertarEventoCanchas(DatosDemostracion.eventoCanchas)
            val fecha = hoy.plusDays(1).toString()
            dao.insertarReserva(ReservaModel("demo1", "vs", "pista", fecha, 9, EstadoReserva.CONFIRMADA, Fechas.semana(hoy)))
            dao.insertarReserva(ReservaModel("demo2", "dm", "estadio", fecha, 10, EstadoReserva.PENDIENTE, Fechas.semana(hoy)))
            notificacion(DatosDemostracion.ADMIN_ID, "Solicitud pendiente", "Diego Morales solicitó el Estadio para ${Fechas.etiqueta(fecha)} a las 10:00.", TipoNotificacion.SOLICITUDES)
        }
    }

    fun observarDatos(): Flow<DatosApp> {
        val reservas = combine(dao.observarUsuarios(), dao.observarCanchas(), dao.observarReservas(), dao.observarPoliticas()) { usuarios, canchas, reservas, politicas ->
            DatosApp(usuarios = usuarios, canchas = canchas, reservas = reservas, politicas = politicas)
        }
        val competencias = combine(dao.observarEventos(), dao.observarEventoCanchas(), dao.observarEquipos(), dao.observarIntegrantes()) { eventos, relaciones, equipos, integrantes ->
            DatosApp(eventos = eventos, eventoCanchas = relaciones, equipos = equipos, integrantes = integrantes)
        }
        val notificaciones = combine(dao.observarEsperas(), dao.observarNotificaciones()) { esperas, notificaciones -> DatosApp(esperas = esperas, notificaciones = notificaciones) }
        return combine(reservas, competencias, notificaciones) { r, c, a ->
            r.copy(eventos = c.eventos, eventoCanchas = c.eventoCanchas, equipos = c.equipos,
                integrantes = c.integrantes, esperas = a.esperas, notificaciones = a.notificaciones)
        }
    }

    suspend fun autenticar(correo: String, clave: String): UsuarioModel {
        exigir(ValidadorLogin.validar(correo, clave) == LoginErrores(), "Revisa los campos de acceso.")
        // Acceso local de demostración. La contraseña real de usuarios requiere un backend.
        val usuario = dao.usuarioPorCorreo(correo.trim().lowercase(Locale.ROOT))
        exigir(usuario != null && clave == DatosDemostracion.CLAVE, "Correo o contraseña de demostración incorrectos.")
        exigir(usuario!!.activo, "Esta cuenta está archivada. Contacta a la administración.")
        return usuario
    }

    suspend fun solicitarReserva(usuarioId: String, formulario: ReservaFormulario): String = db.withTransaction {
        val usuario = usuarioActivo(usuarioId)
        val cancha = dao.cancha(formulario.canchaId) ?: throw ReglaNegocioException("Selecciona una instalación.")
        val politica = dao.politica(usuario.grado) ?: throw ReglaNegocioException("No hay una política para tu grado.")
        val error = ReglasReserva.errorSolicitud(usuario, cancha, politica, formulario, dao.reservas(), dao.eventos(), dao.eventoCanchas(), clock)
        exigir(error == null, error ?: "No fue posible reservar.")
        val nuevaId = id()
        val estado = if (politica.requiereAprobacion) EstadoReserva.PENDIENTE else EstadoReserva.CONFIRMADA
        dao.insertarReserva(ReservaModel(nuevaId, usuario.id, cancha.id, formulario.fecha, formulario.horaInicio,
            estado, Fechas.semana(Fechas.hoy(clock))))
        notificacion(usuarioId, if (estado == EstadoReserva.PENDIENTE) "Solicitud enviada" else "Reserva confirmada", "${cancha.nombre} · ${Fechas.etiqueta(formulario.fecha)} · ${Fechas.horario(formulario.horaInicio)}", TipoNotificacion.SOLICITUDES)
        if (estado == EstadoReserva.PENDIENTE) notificacion(DatosDemostracion.ADMIN_ID, "Nueva solicitud", "${usuario.nombre} · ${cancha.nombre}", TipoNotificacion.SOLICITUDES)
        nuevaId
    }

    suspend fun cancelarReserva(actorId: String, reservaId: String) = db.withTransaction {
        val actor = usuarioActivo(actorId)
        val reserva = dao.reserva(reservaId) ?: throw ReglaNegocioException("No se encontró la reserva.")
        exigir(actor.rol == Rol.ADMIN || actor.id == reserva.usuarioId, "Solo puedes cancelar tus propias reservas.")
        exigir(reserva.estado == EstadoReserva.CONFIRMADA && Fechas.futuro(reserva.fecha, reserva.horaInicio, clock), "Solo se cancelan reservas futuras confirmadas.")
        dao.guardarReserva(reserva.copy(estado = EstadoReserva.CANCELADA))
        notificacion(reserva.usuarioId, "Reserva cancelada", "El bloque quedó libre. El token se libera en la semana en que se solicitó.", TipoNotificacion.RESERVAS)
        if (actor.rol == Rol.USUARIO) notificacion(DatosDemostracion.ADMIN_ID, "Reserva cancelada por el titular", "El bloque de ${reserva.fecha} quedó libre.", TipoNotificacion.RESERVAS)
        promoverEspera(reserva.canchaId, reserva.fecha, reserva.horaInicio)
    }

    suspend fun resolverSolicitud(adminId: String, reservaId: String, aprobar: Boolean) = db.withTransaction {
        administrador(adminId)
        val reserva = dao.reserva(reservaId) ?: throw ReglaNegocioException("No se encontró la solicitud.")
        exigir(reserva.estado == EstadoReserva.PENDIENTE, "La solicitud ya fue procesada.")
        exigir(!aprobar || Fechas.futuro(reserva.fecha, reserva.horaInicio, clock), "La solicitud está vencida; puedes rechazarla.")
        if (aprobar) {
            usuarioActivo(reserva.usuarioId)
            val cancha = dao.cancha(reserva.canchaId)
            exigir(cancha?.activa == true && cancha.estado == EstadoCancha.HABILITADA, "La instalación no está habilitada.")
            exigir(cancha!!.permite(reserva.fecha, reserva.horaInicio), "La instalación está cerrada en ese bloque.")
            exigir(!ReglasReserva.bloqueadoPorEvento(reserva.canchaId, reserva.fecha, reserva.horaInicio, dao.eventos(), dao.eventoCanchas()), "El bloque está ocupado por un evento.")
        }
        dao.guardarReserva(reserva.copy(estado = if (aprobar) EstadoReserva.CONFIRMADA else EstadoReserva.RECHAZADA))
        notificacion(reserva.usuarioId, if (aprobar) "Reserva confirmada" else "Solicitud rechazada", if (aprobar) "Tu solicitud fue aprobada." else "Tu solicitud fue rechazada. El token se liberó.", TipoNotificacion.SOLICITUDES)
        if (!aprobar) promoverEspera(reserva.canchaId, reserva.fecha, reserva.horaInicio)
    }

    suspend fun reprogramarReserva(adminId: String, reservaId: String, fecha: String, hora: Int) = db.withTransaction {
        administrador(adminId)
        val r = dao.reserva(reservaId) ?: throw ReglaNegocioException("No se encontró la reserva.")
        exigir(r.ocupaHorario && Fechas.futuro(r.fecha, r.horaInicio, clock), "Solo se modifican reservas futuras activas.")
        val usuario = usuarioActivo(r.usuarioId)
        val cancha = dao.cancha(r.canchaId) ?: throw ReglaNegocioException("Instalación no encontrada.")
        val politica = dao.politica(usuario.grado) ?: throw ReglaNegocioException("Política no encontrada.")
        val f = ReservaFormulario(r.canchaId, fecha = fecha, horaInicio = hora, aceptaNormas = true)
        val error = ReglasReserva.errorSolicitud(usuario, cancha, politica.copy(tokensSemana = Int.MAX_VALUE), f,
            dao.reservas().filter { it.id != r.id }, dao.eventos(), dao.eventoCanchas(), clock)
        exigir(error == null, error ?: "No se pudo cambiar el bloque.")
        dao.guardarReserva(r.copy(fecha = fecha, horaInicio = hora))
        if (fecha != r.fecha || hora != r.horaInicio) promoverEspera(r.canchaId, r.fecha, r.horaInicio)
        notificacion(r.usuarioId, "Reserva modificada", "Nuevo bloque: ${Fechas.etiqueta(fecha)} · ${Fechas.horario(hora)}", TipoNotificacion.RESERVAS)
    }

    suspend fun alternarEspera(usuarioId: String, f: ReservaFormulario): String = db.withTransaction {
        val usuario = usuarioActivo(usuarioId)
        exigir(usuario.rol == Rol.USUARIO, "La administración no participa en la lista de espera.")
        val lista = dao.espera(f.canchaId, f.fecha, f.horaInicio)
        val anterior = lista.firstOrNull { it.usuarioId == usuarioId }
        if (anterior != null) {
            dao.eliminarEspera(anterior.id)
            "Saliste de la lista de espera."
        } else {
            val politica = dao.politica(usuario.grado) ?: throw ReglaNegocioException("No existe una política para tu grado.")
            val cancha = dao.cancha(f.canchaId) ?: throw ReglaNegocioException("Instalación no encontrada.")
            val errores = ReglasReserva.validarFormulario(f, Fechas.hoy(clock), politica.anticipacionDias, exigirNormas = false)
            exigir(errores == ReservaErrores(), errores.fecha ?: "Revisa la solicitud.")
            exigir(cancha.activa && cancha.estado == EstadoCancha.HABILITADA && cancha.permite(f.fecha, f.horaInicio), "La instalación está cerrada en este bloque.")
            exigir(Fechas.futuro(f.fecha, f.horaInicio, clock), "El bloque ya comenzó.")
            exigir(!ReglasReserva.bloqueadoPorEvento(cancha.id, f.fecha, f.horaInicio, dao.eventos(), dao.eventoCanchas()), "Un evento institucional bloquea este horario.")
            val reservas = dao.reservas()
            exigir(reservas.any { it.canchaId == f.canchaId && it.fecha == f.fecha && it.horaInicio == f.horaInicio && it.ocupaHorario }, "El horario está libre; puedes reservarlo directamente.")
            exigir(reservas.none { it.usuarioId == usuarioId && it.fecha == f.fecha && it.horaInicio == f.horaInicio && it.ocupaHorario }, "Ya tienes una reserva en este horario.")
            // Orden estricto incluso si dos ingresos tienen la misma marca de milisegundo.
            val orden = maxOf(clock.millis(), (lista.lastOrNull()?.creadaEn ?: 0L) + 1L)
            dao.insertarEspera(EsperaModel(id(), usuarioId, f.canchaId, f.fecha, f.horaInicio, orden))
            "Te uniste a la lista de espera. No se consume un token hasta obtener el cupo."
        }
    }

    private suspend fun promoverEspera(canchaId: String, fecha: String, hora: Int) {
        if (!Fechas.futuro(fecha, hora, clock)) return
        val cancha = dao.cancha(canchaId) ?: return
        for (espera in dao.espera(canchaId, fecha, hora)) {
            val usuario = dao.usuario(espera.usuarioId)
            val politica = usuario?.let { dao.politica(it.grado) }
            val f = ReservaFormulario(canchaId, fecha = fecha, horaInicio = hora, aceptaNormas = true)
            val error = if (usuario == null || politica == null) "Cuenta no disponible." else
                ReglasReserva.errorSolicitud(usuario, cancha, politica, f, dao.reservas(), dao.eventos(), dao.eventoCanchas(), clock)
            dao.eliminarEspera(espera.id)
            if (error == null && usuario != null && politica != null) {
                dao.insertarReserva(ReservaModel(id(), usuario.id, canchaId, fecha, hora,
                    if (politica.requiereAprobacion) EstadoReserva.PENDIENTE else EstadoReserva.CONFIRMADA, Fechas.semana(Fechas.hoy(clock))))
                notificacion(usuario.id, "Cupo asignado desde lista de espera", "${cancha.nombre} · ${Fechas.etiqueta(fecha)} · ${Fechas.horario(hora)}. Se consumió un token.", TipoNotificacion.LISTA_ESPERA)
                notificacion(DatosDemostracion.ADMIN_ID, "Cupo liberado asignado", "${usuario.nombre} recibió el cupo de ${cancha.nombre}.", TipoNotificacion.LISTA_ESPERA)
                break
            } else notificacion(espera.usuarioId, "Lista de espera procesada", "No fue posible asignarte el cupo: $error", TipoNotificacion.LISTA_ESPERA)
        }
    }

    suspend fun guardarCancha(adminId: String, f: CanchaFormulario) = db.withTransaction {
        administrador(adminId)
        val errores = ValidadorCancha.validar(f)
        exigir(errores == CanchaErrores(), errores.nombre ?: errores.capacidad ?: errores.horario ?: "Revisa la instalación.")
        val anterior = f.id.takeIf { it.isNotBlank() }?.let { dao.cancha(it) }
        val futuras = dao.reservas().filter { it.canchaId == f.id && it.ocupaHorario && Fechas.futuro(it.fecha, it.horaInicio, clock) }
        val relaciones = dao.eventoCanchas()
        val eventos = dao.eventos().filter { e -> e.activo && Fechas.futuro(e.fecha, e.horaInicio, clock) && relaciones.any { it.eventoId == e.id && it.canchaId == f.id } }
        exigir(f.estado == EstadoCancha.HABILITADA || (futuras.isEmpty() && eventos.isEmpty()), "Cancela o reprograma primero las reservas y eventos futuros de esta instalación.")
        val propuesta = CanchaModel(f.id.ifBlank { id() }, f.nombre.trim(), f.tipo, f.capacidad.toInt(), f.estado,
            f.apertura.toInt(), f.cierre.toInt(), f.observaciones.trim(), anterior?.activa ?: true,
            Horarios.mascara(f.dias.map { it - 1 }.toSet()), Horarios.mascara(f.bloques))
        exigir(futuras.all { propuesta.permite(it.fecha, it.horaInicio) }, "El nuevo horario deja reservas fuera de los bloques permitidos.")
        exigir(eventos.all { e -> (e.horaInicio until e.horaFin).all { propuesta.permite(e.fecha, it) } }, "El nuevo horario interfiere con un evento futuro.")
        dao.guardarCancha(propuesta)
    }

    suspend fun archivarCancha(adminId: String, canchaId: String, archivar: Boolean) = db.withTransaction {
        administrador(adminId)
        val cancha = dao.cancha(canchaId) ?: throw ReglaNegocioException("No se encontró la instalación.")
        if (archivar) {
            exigir(dao.reservas().none { it.canchaId == canchaId && it.ocupaHorario && Fechas.futuro(it.fecha, it.horaInicio, clock) }, "Hay reservas futuras. Cancélalas antes de archivar.")
            val relaciones = dao.eventoCanchas()
            exigir(dao.eventos().none { e -> e.activo && Fechas.futuro(e.fecha, e.horaInicio, clock) && relaciones.any { it.eventoId == e.id && it.canchaId == canchaId } }, "Hay un evento futuro en esta instalación.")
        }
        dao.guardarCancha(cancha.copy(activa = !archivar))
    }

    suspend fun guardarPoliticas(adminId: String, politicas: List<PoliticaModel>) = db.withTransaction {
        administrador(adminId)
        exigir(politicas.map { it.grado }.toSet() == Grado.entries.toSet(), "Debes definir una política para cada grado.")
        exigir(politicas.all { it.tokensSemana in 1..20 && it.anticipacionDias in 1..30 }, "Usa entre 1 y 20 tokens y entre 1 y 30 días de anticipación.")
        dao.guardarPoliticas(politicas)
    }

    suspend fun cambiarEstadoUsuario(adminId: String, usuarioId: String, activo: Boolean) = db.withTransaction {
        administrador(adminId)
        val usuario = dao.usuario(usuarioId) ?: throw ReglaNegocioException("Usuario no encontrado.")
        exigir(usuario.rol == Rol.USUARIO, "Esta pantalla solo administra cuentas de usuarios.")
        dao.guardarUsuario(usuario.copy(activo = activo))
        if (!activo) {
            dao.reservas().filter { it.usuarioId == usuarioId && it.ocupaHorario && Fechas.futuro(it.fecha, it.horaInicio, clock) }.forEach { r ->
                dao.guardarReserva(r.copy(estado = EstadoReserva.CANCELADA))
                promoverEspera(r.canchaId, r.fecha, r.horaInicio)
            }
        }
        notificacion(usuarioId, if (activo) "Cuenta restaurada" else "Cuenta archivada", "La administración cambió el estado de tu cuenta.", TipoNotificacion.CUENTA)
    }

    suspend fun crearEvento(adminId: String, f: EventoFormulario) = db.withTransaction {
        administrador(adminId)
        val errores = ValidadorEvento.validar(f, Fechas.hoy(clock))
        exigir(errores == EventoErrores(), errores.nombre ?: errores.fecha ?: errores.horario ?: errores.equipos ?: errores.instalaciones ?: "Revisa el evento.")
        val inicio = f.inicio.toInt()
        val fin = f.fin.toInt()
        exigir(Fechas.futuro(f.fecha, inicio, clock), "El horario de inicio ya pasó.")
        val reservas = dao.reservas()
        val eventos = dao.eventos()
        val relaciones = dao.eventoCanchas()
        for (canchaId in f.canchas) {
            val cancha = dao.cancha(canchaId) ?: throw ReglaNegocioException("Instalación no encontrada.")
            exigir(cancha.activa && cancha.estado == EstadoCancha.HABILITADA, "${cancha.nombre} no está disponible.")
            exigir((inicio until fin).all { cancha.permite(f.fecha, it) }, "El evento queda fuera del horario de ${cancha.nombre}.")
            exigir(reservas.none { it.canchaId == canchaId && it.fecha == f.fecha && it.ocupaHorario && it.horaInicio in inicio until fin }, "Hay reservas en ${cancha.nombre}. Reprográmalas antes de crear el evento.")
            exigir((inicio until fin).none { ReglasReserva.bloqueadoPorEvento(canchaId, f.fecha, it, eventos, relaciones) }, "Otro evento ocupa ${cancha.nombre} en ese horario.")
        }
        val eventoId = id()
        dao.guardarEvento(EventoModel(eventoId, f.nombre.trim(), f.disciplina, f.fecha, inicio, fin, f.cupos.toInt(), f.minimo.toInt(), f.descripcion.trim()))
        dao.insertarEventoCanchas(f.canchas.map { EventoCanchaModel(eventoId, it) })
        dao.usuarios().filter { it.activo }.forEach { notificacion(it.id, "Nuevo evento", "${f.nombre.trim()} · ${Fechas.etiqueta(f.fecha)}", TipoNotificacion.EVENTOS) }
    }

    suspend fun cancelarEvento(adminId: String, eventoId: String) = db.withTransaction {
        administrador(adminId)
        val evento = dao.evento(eventoId) ?: throw ReglaNegocioException("Evento no encontrado.")
        exigir(evento.activo && Fechas.futuro(evento.fecha, evento.horaInicio, clock), "Solo se cancelan eventos futuros activos.")
        dao.guardarEvento(evento.copy(activo = false))
        dao.equipos(eventoId).forEach { notificacion(it.capitanId, "Evento cancelado", evento.nombre, TipoNotificacion.EVENTOS) }
    }

    suspend fun crearEquipo(usuarioId: String, f: EquipoFormulario) = db.withTransaction {
        val usuario = usuarioActivo(usuarioId)
        exigir(usuario.rol == Rol.USUARIO, "Solo los usuarios pueden inscribir un equipo.")
        val evento = dao.evento(f.eventoId) ?: throw ReglaNegocioException("Selecciona un evento.")
        exigir(evento.activo && Fechas.futuro(evento.fecha, evento.horaInicio, clock), "Las inscripciones están cerradas.")
        exigir(f.nombre.trim().length >= 3, "El nombre del equipo debe tener al menos 3 caracteres.")
        val miembros = f.integrantes + usuarioId
        exigir(miembros.size >= evento.minimoIntegrantes, "El equipo necesita al menos ${evento.minimoIntegrantes} integrantes.")
        val equipos = dao.equipos(evento.id)
        exigir(equipos.size < evento.cupoEquipos, "El evento ya completó su cupo de equipos.")
        exigir(equipos.none { it.nombre.equals(f.nombre.trim(), ignoreCase = true) }, "Ya existe un equipo con ese nombre en el evento.")
        val idsEquipos = equipos.map { it.id }.toSet()
        exigir(dao.integrantes().none { it.equipoId in idsEquipos && it.usuarioId in miembros }, "Un integrante ya está inscrito en otro equipo de este evento.")
        for (miembroId in miembros) {
            val miembro = usuarioActivo(miembroId)
            exigir(miembro.rol == Rol.USUARIO && miembro.compania == usuario.compania, "Todos los integrantes deben ser usuarios activos de tu compañía.")
        }
        val equipoId = id()
        dao.insertarEquipo(EquipoModel(equipoId, evento.id, usuario.id, f.nombre.trim(), usuario.compania))
        dao.insertarIntegrantes(miembros.map { IntegranteModel(equipoId, it) })
        notificacion(usuario.id, "Equipo inscrito", "${f.nombre.trim()} · ${evento.nombre}", TipoNotificacion.EVENTOS)
    }

    suspend fun retirarEquipo(actorId: String, equipo: EquipoModel) = db.withTransaction {
        val actor = usuarioActivo(actorId)
        val actual = dao.equipos(equipo.eventoId).firstOrNull { it.id == equipo.id } ?: throw ReglaNegocioException("El equipo ya fue retirado.")
        exigir(actor.rol == Rol.ADMIN || actual.capitanId == actor.id, "Solo el capitán o la administración puede retirar el equipo.")
        val evento = dao.evento(actual.eventoId) ?: throw ReglaNegocioException("Evento no encontrado.")
        exigir(Fechas.futuro(evento.fecha, evento.horaInicio, clock), "La competencia ya comenzó.")
        dao.eliminarIntegrantes(actual.id)
        dao.eliminarEquipo(actual.id)
        notificacion(actual.capitanId, "Equipo retirado", actual.nombre, TipoNotificacion.EVENTOS)
    }
}
