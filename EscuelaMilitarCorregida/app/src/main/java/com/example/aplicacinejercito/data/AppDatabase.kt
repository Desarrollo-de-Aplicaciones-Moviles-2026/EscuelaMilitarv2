package com.example.aplicacinejercito.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aplicacinejercito.model.*

@Database(
    entities = [UsuarioModel::class, CanchaModel::class, ReservaModel::class, PoliticaModel::class,
        EventoModel::class, EventoCanchaModel::class, EquipoModel::class, IntegranteModel::class,
        EsperaModel::class, NotificacionModel::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
