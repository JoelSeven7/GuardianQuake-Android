package com.guardianquake.app.data.remote.dto

data class DispositivoCrearRequest(
    val modelo: String,
    val fabricante: String,
    val so_version: String?
)

data class DispositivoOut(
    val id_dispositivo: Int,
    val modelo: String,
    val fabricante: String,
    val so_version: String?,
    val sensibilidad_recomendada: Double,
    val fecha_registro: String
)

data class CalibracionOut(
    val id_dispositivo: Int,
    val modelo: String,
    val fabricante: String,
    val sensibilidad_recomendada: Double
)
