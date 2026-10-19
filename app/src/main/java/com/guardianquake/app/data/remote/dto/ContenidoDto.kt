package com.guardianquake.app.data.remote.dto

data class ContenidoEducativoOut(
    val id_contenido: Int,
    val titulo: String,
    val cuerpo: String,
    val version: String,
    val fecha_publicacion: String
)
