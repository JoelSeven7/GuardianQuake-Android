package com.guardianquake.app.data.remote.dto

data class VerificarCompraRequest(
    val id_transaccion: String,
    val plataforma: String = "GOOGLE_PLAY"
)

data class SuscripcionOut(
    val id_suscripcion: Int,
    val tipo: String,
    val fecha_inicio: String,
    val fecha_fin: String?,
    val estado: String,
    val plataforma: String
)

data class EstadoSuscripcionOut(
    val tipo_cuenta: String,
    val suscripcion: SuscripcionOut?
)
