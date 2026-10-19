package com.guardianquake.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoLocalDao {

    @Insert
    suspend fun insertar(evento: EventoLocalEntity): Long

    @Update
    suspend fun actualizar(evento: EventoLocalEntity)

    @Query("SELECT * FROM eventos_locales ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<EventoLocalEntity>>

    @Query("SELECT * FROM eventos_locales WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EventoLocalEntity>
}
