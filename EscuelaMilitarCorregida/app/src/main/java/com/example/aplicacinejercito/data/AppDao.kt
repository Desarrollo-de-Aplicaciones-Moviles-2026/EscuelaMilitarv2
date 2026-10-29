package com.example.aplicacinejercito.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.aplicacinejercito.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM usuarios ORDER BY nombre") fun observarUsuarios(): Flow<List<UsuarioModel>>
    @Query("SELECT * FROM canchas ORDER BY nombre") fun observarCanchas(): Flow<List<CanchaModel>>
    @Query("SELECT * FROM reservas ORDER BY fecha, horaInicio") fun observarReservas(): Flow<List<ReservaModel>>
    @Query("SELECT * FROM politicas") fun observarPoliticas(): Flow<List<PoliticaModel>>
    @Query("SELECT * FROM eventos ORDER BY fecha, horaInicio") fun observarEventos(): Flow<List<EventoModel>>
    @Query("SELECT * FROM evento_canchas") fun observarEventoCanchas(): Flow<List<EventoCanchaModel>>
    @Query("SELECT * FROM equipos") fun observarEquipos(): Flow<List<EquipoModel>>
    @Query("SELECT * FROM equipo_integrantes") fun observarIntegrantes(): Flow<List<IntegranteModel>>
    @Query("SELECT * FROM espera ORDER BY creadaEn, id") fun observarEsperas(): Flow<List<EsperaModel>>
    @Query("SELECT * FROM notificaciones ORDER BY creadoEn DESC") fun observarNotificaciones(): Flow<List<NotificacionModel>>

    @Query("SELECT * FROM usuarios WHERE id = :id") suspend fun usuario(id: String): UsuarioModel?
    @Query("SELECT * FROM usuarios WHERE correo = :correo") suspend fun usuarioPorCorreo(correo: String): UsuarioModel?
    @Query("SELECT * FROM usuarios") suspend fun usuarios(): List<UsuarioModel>
    @Query("SELECT COUNT(*) FROM usuarios") suspend fun cantidadUsuarios(): Int
    @Query("SELECT * FROM canchas WHERE id = :id") suspend fun cancha(id: String): CanchaModel?
    @Query("SELECT * FROM politicas WHERE grado = :grado") suspend fun politica(grado: Grado): PoliticaModel?
    @Query("SELECT * FROM reservas") suspend fun reservas(): List<ReservaModel>
    @Query("SELECT * FROM reservas WHERE id = :id") suspend fun reserva(id: String): ReservaModel?
    @Query("SELECT * FROM eventos") suspend fun eventos(): List<EventoModel>
    @Query("SELECT * FROM eventos WHERE id = :id") suspend fun evento(id: String): EventoModel?
    @Query("SELECT * FROM evento_canchas") suspend fun eventoCanchas(): List<EventoCanchaModel>
    @Query("SELECT * FROM equipos WHERE eventoId = :eventoId") suspend fun equipos(eventoId: String): List<EquipoModel>
    @Query("SELECT * FROM equipo_integrantes") suspend fun integrantes(): List<IntegranteModel>
    @Query("SELECT * FROM espera WHERE canchaId = :canchaId AND fecha = :fecha AND horaInicio = :hora ORDER BY creadaEn, id")
    suspend fun espera(canchaId: String, fecha: String, hora: Int): List<EsperaModel>

    @Insert suspend fun insertarUsuarios(usuarios: List<UsuarioModel>)
    @Insert suspend fun insertarCanchas(canchas: List<CanchaModel>)
    @Insert suspend fun insertarReserva(reserva: ReservaModel)
    @Insert suspend fun insertarEventos(eventos: List<EventoModel>)
    @Insert suspend fun insertarEventoCanchas(relaciones: List<EventoCanchaModel>)
    @Insert suspend fun insertarEquipo(equipo: EquipoModel)
    @Insert suspend fun insertarIntegrantes(integrantes: List<IntegranteModel>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertarEspera(espera: EsperaModel)
    @Insert suspend fun insertarNotificacion(notificacion: NotificacionModel)
    @Upsert suspend fun guardarUsuario(usuario: UsuarioModel)
    @Upsert suspend fun guardarCancha(cancha: CanchaModel)
    @Upsert suspend fun guardarReserva(reserva: ReservaModel)
    @Upsert suspend fun guardarPoliticas(politicas: List<PoliticaModel>)
    @Upsert suspend fun guardarEvento(evento: EventoModel)
    @Query("DELETE FROM espera WHERE id = :id") suspend fun eliminarEspera(id: String)
    @Query("DELETE FROM equipo_integrantes WHERE equipoId = :equipoId") suspend fun eliminarIntegrantes(equipoId: String)
    @Query("DELETE FROM equipos WHERE id = :id") suspend fun eliminarEquipo(id: String)
}
