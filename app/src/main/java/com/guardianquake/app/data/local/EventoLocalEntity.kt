package com.guardianquake.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos_locales")
data class EventoLocalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaHora: String,
    val intensidadAproximada: Double,
    val duracionMs: Int,
    val sensibilidadUsada: Int,
    val sincronizado: Boolean = false
)
