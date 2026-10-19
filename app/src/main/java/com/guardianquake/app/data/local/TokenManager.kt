package com.guardianquake.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "guardianquake_prefs")

class TokenManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey("jwt_token")
    private val deviceIdKey = stringPreferencesKey("id_dispositivo")
    private val sonidoAlarmaUriKey = stringPreferencesKey("sonido_alarma_uri")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }

    suspend fun guardarToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun obtenerTokenSync(): String? {
        return context.dataStore.data.first()[tokenKey]
    }

    suspend fun guardarIdDispositivo(id: Int) {
        context.dataStore.edit { it[deviceIdKey] = id.toString() }
    }

    suspend fun obtenerIdDispositivoSync(): Int? {
        return context.dataStore.data.first()[deviceIdKey]?.toIntOrNull()
    }

    /** RF05: sonido de alarma elegido por el usuario (archivo de su
     *  telefono). Si es null, se usa la sirena predeterminada de la app. */
    suspend fun guardarSonidoAlarmaUri(uri: String?) {
        context.dataStore.edit {
            if (uri == null) it.remove(sonidoAlarmaUriKey) else it[sonidoAlarmaUriKey] = uri
        }
    }

    suspend fun obtenerSonidoAlarmaUriSync(): String? {
        return context.dataStore.data.first()[sonidoAlarmaUriKey]
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }
}
