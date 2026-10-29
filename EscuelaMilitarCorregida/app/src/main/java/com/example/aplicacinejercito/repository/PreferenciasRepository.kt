package com.example.aplicacinejercito.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.aplicacinejercito.model.*
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.preferenciasDataStore by preferencesDataStore(name = "preferencias")

class PreferenciasRepository(context: Context) {
    private val store = context.applicationContext.preferenciasDataStore
    private val tema = booleanPreferencesKey("tema_oscuro")
    val preferencias = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { prefs ->
        val valores = prefs.asMap()
        PreferenciasModel(
            temaOscuro = prefs[tema] ?: false,
            // Respeta la preferencia general de la versión anterior hasta que cada cuenta la cambie.
            notificacionesPredeterminadas = prefs[booleanPreferencesKey("mostrar_avisos")] ?: true,
            notificacionesPorUsuario = valores.entries.filter { it.key.name.startsWith("notificaciones_") }
                .associate { it.key.name.removePrefix("notificaciones_") to (it.value == true) },
            tiposSilenciados = valores.entries.filter { it.key.name.startsWith("silenciados_") }
                .associate { entrada -> entrada.key.name.removePrefix("silenciados_") to
                    (entrada.value as? Set<*>) .orEmpty().mapNotNull { nombre -> TipoNotificacion.entries.firstOrNull { it.name == nombre } }.toSet() },
            fotos = valores.entries.filter { it.key.name.startsWith("foto_") }
                .associate { it.key.name.removePrefix("foto_") to it.value.toString() }
        )
    }
    suspend fun cambiarTema(oscuro: Boolean) { store.edit { it[tema] = oscuro } }
    suspend fun cambiarNotificaciones(usuarioId: String, activas: Boolean) {
        store.edit { it[booleanPreferencesKey("notificaciones_$usuarioId")] = activas }
    }
    suspend fun cambiarTipo(usuarioId: String, tipo: TipoNotificacion, activo: Boolean) {
        val clave = stringSetPreferencesKey("silenciados_$usuarioId")
        store.edit { prefs ->
            val anteriores = prefs[clave].orEmpty()
            prefs[clave] = if (activo) anteriores - tipo.name else anteriores + tipo.name
        }
    }
    suspend fun guardarFoto(usuarioId: String, uri: String) { store.edit { it[stringPreferencesKey("foto_$usuarioId")] = uri } }
}
