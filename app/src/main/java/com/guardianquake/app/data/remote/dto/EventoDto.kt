package com.guardianquake.app.data.remote.dto

data class EventoSismicoRequest(
    val id_dispositivo: Int?,
    val fecha_hora: String,
    val intensidad_aproximada: Double,
    val duracion_ms: Int,
    val sensibilidad_usada: Int
)

data class EventoSismicoOut(
    val id_evento: Int,
    val id_dispositivo: Int?,
    val fecha_hora: String,
    val intensidad_aproximada: Double,
    val duracion_ms: Int,
    val sensibilidad_usada: Int,
    val fecha_registro: String
)
