package com.guardianquake.app.data.remote.dto

data class UsuarioRegistroRequest(
    val correo: String,
    val password: String
)

data class UsuarioOut(
    val id_usuario: Int,
    val correo: String,
    val tipo_cuenta: String,
    val fecha_registro: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)
