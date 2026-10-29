package com.example.aplicacinejercito.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Migración explícita: elimina solo los campos retirados y conserva IDs, estados e historial. */
object Migraciones {
    internal val consultas1a2 = listOf(
        """CREATE TABLE reservas_nueva (id TEXT NOT NULL, usuarioId TEXT NOT NULL, canchaId TEXT NOT NULL, fecha TEXT NOT NULL, horaInicio INTEGER NOT NULL, estado TEXT NOT NULL, semanaSolicitud TEXT NOT NULL, PRIMARY KEY(id))""",
        """INSERT INTO reservas_nueva SELECT id, usuarioId, canchaId, fecha, horaInicio, estado, semanaSolicitud FROM reservas""",
        """DROP TABLE reservas""",
        """ALTER TABLE reservas_nueva RENAME TO reservas""",
        """CREATE INDEX index_reservas_canchaId_fecha_horaInicio ON reservas (canchaId, fecha, horaInicio)""",
        """CREATE TABLE espera_nueva (id TEXT NOT NULL, usuarioId TEXT NOT NULL, canchaId TEXT NOT NULL, fecha TEXT NOT NULL, horaInicio INTEGER NOT NULL, creadaEn INTEGER NOT NULL, PRIMARY KEY(id))""",
        """INSERT INTO espera_nueva SELECT id, usuarioId, canchaId, fecha, horaInicio, creadaEn FROM espera""",
        """DROP TABLE espera""",
        """ALTER TABLE espera_nueva RENAME TO espera""",
        """CREATE UNIQUE INDEX index_espera_usuarioId_canchaId_fecha_horaInicio ON espera (usuarioId, canchaId, fecha, horaInicio)""",
        """CREATE TABLE notificaciones (id TEXT NOT NULL, usuarioId TEXT NOT NULL, titulo TEXT NOT NULL, detalle TEXT NOT NULL, creadoEn INTEGER NOT NULL, tipo TEXT NOT NULL, PRIMARY KEY(id))""",
        """INSERT INTO notificaciones SELECT id, usuarioId, titulo, CASE WHEN titulo = 'Solicitud rechazada' THEN 'Tu solicitud fue rechazada. El token se liberó.' ELSE detalle END, creadoEn, CASE WHEN titulo LIKE '%espera%' OR titulo LIKE '%liberado asignado%' THEN 'LISTA_ESPERA' WHEN titulo LIKE '%quipo%' OR titulo LIKE '%vento%' THEN 'EVENTOS' WHEN titulo LIKE '%uenta%' THEN 'CUENTA' WHEN titulo LIKE '%olicitud%' THEN 'SOLICITUDES' ELSE 'RESERVAS' END FROM avisos""",
        """DROP TABLE avisos""",
        """ALTER TABLE canchas ADD COLUMN diasActivos INTEGER NOT NULL DEFAULT 127""",
        """ALTER TABLE canchas ADD COLUMN bloquesDisponibles INTEGER NOT NULL DEFAULT 261888""",
        """UPDATE canchas SET horaCierre = 18 WHERE horaApertura = 8 AND horaCierre = 20 AND NOT EXISTS (SELECT 1 FROM reservas r WHERE r.canchaId = canchas.id AND r.estado IN ('PENDIENTE','CONFIRMADA') AND r.horaInicio >= 18) AND NOT EXISTS (SELECT 1 FROM eventos e JOIN evento_canchas ec ON e.id = ec.eventoId WHERE ec.canchaId = canchas.id AND e.activo = 1 AND e.horaFin > 18)""",
        """UPDATE canchas SET bloquesDisponibles = (1 << horaCierre) - (1 << horaApertura)""",
        """UPDATE canchas SET activa = 0 WHERE id IN ('rugby1', 'piscina', 'escalada')""",
        """UPDATE canchas SET nombre = 'Pista Atlética', tipo = 'Pista de atletismo' WHERE id = 'pista'""",
        """UPDATE canchas SET nombre = 'Sala de Musculación', tipo = 'Sala de musculación' WHERE id = 'muscu'""",
        """UPDATE canchas SET nombre = 'Gimnasio de Básquetbol', tipo = 'Gimnasio de básquetbol' WHERE id = 'basquet'""",
        """UPDATE canchas SET nombre = 'Gimnasio de Vóleibol', tipo = 'Gimnasio de vóleibol' WHERE id = 'voley'""",
        """UPDATE canchas SET nombre = 'Estadio', tipo = 'Estadio multiuso' WHERE id = 'estadio'""",
        """UPDATE canchas SET nombre = 'Cancha de Rugby N.° 2', tipo = 'Cancha de rugby' WHERE id = 'rugby2'""",
        """UPDATE canchas SET nombre = 'Cancha de Fútbol N.° 2', tipo = 'Cancha de fútbol' WHERE id = 'futbol1'""",
        """UPDATE canchas SET nombre = 'Cancha de Tenis N.° 1', tipo = 'Cancha de tenis' WHERE id = 'tenis'""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'tenis2','Cancha de Tenis N.° 2','Cancha de tenis',8,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'tenis3','Cancha de Tenis N.° 3','Cancha de tenis',8,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'tenis4','Cancha de Tenis N.° 4','Cancha de tenis',8,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'tenis5','Cancha de Tenis N.° 5','Cancha de tenis',8,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'tenis6','Cancha de Tenis N.° 6','Cancha de tenis',8,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """UPDATE canchas SET nombre = 'Cancha de Baby Fútbol', tipo = 'Cancha de baby fútbol' WHERE id = 'baby'""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'futbolito','Cancha de Futbolito','Cancha de futbolito',14,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """UPDATE canchas SET nombre = 'Multicancha N.° 1', tipo = 'Multicancha techada' WHERE id = 'mc1'""",
        """UPDATE canchas SET nombre = 'Multicancha N.° 2', tipo = 'Multicancha techada' WHERE id = 'mc2'""",
        """UPDATE canchas SET nombre = 'Tatami para disciplinas de combate', tipo = 'Tatami' WHERE id = 'combate'""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'aula','Aula Magna','Auditorio institucional',300,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)""",
        """INSERT OR IGNORE INTO canchas (id,nombre,tipo,capacidad,estado,horaApertura,horaCierre,observaciones,activa,diasActivos,bloquesDisponibles) SELECT 'obstaculos','Cancha de Obstáculos de Pentatlón Militar','Pentatlón militar',30,'HABILITADA',8,18,'',1,127,261888 WHERE EXISTS (SELECT 1 FROM usuarios)"""
    )
    val MIGRACION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            consultas1a2.forEach(db::execSQL)
        }
    }
}
