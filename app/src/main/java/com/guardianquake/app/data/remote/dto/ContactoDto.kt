package com.guardianquake.app.data.remote.dto

data class ContactoEmergenciaRequest(
    val nombre: String,
    val telefono: String
)

data class ContactoEmergenciaOut(
    val id_contacto: Int,
    val nombre: String,
    val telefono: String
)
