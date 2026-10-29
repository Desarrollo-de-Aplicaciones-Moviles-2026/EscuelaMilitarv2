package com.example.aplicacinejercito

import android.app.Application
import androidx.room.Room
import com.example.aplicacinejercito.data.AppDatabase
import com.example.aplicacinejercito.data.Migraciones
import com.example.aplicacinejercito.repository.DeportesRepository
import com.example.aplicacinejercito.repository.PreferenciasRepository
import com.example.aplicacinejercito.viewmodel.AppViewModelFactory

class DeportesApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "gestion_deportiva.db").addMigrations(Migraciones.MIGRACION_1_2).build()
    }
    val factory by lazy { AppViewModelFactory(DeportesRepository(database), PreferenciasRepository(this)) }
}
