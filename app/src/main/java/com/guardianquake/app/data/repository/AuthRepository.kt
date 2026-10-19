package com.guardianquake.app.data.repository

import com.guardianquake.app.data.local.TokenManager
import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.TokenResponse
import com.guardianquake.app.data.remote.dto.UsuarioOut
import com.guardianquake.app.data.remote.dto.UsuarioRegistroRequest

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun registro(correo: String, password: String): ApiResult<UsuarioOut> =
        ejecutarLlamada { api.registro(UsuarioRegistroRequest(correo, password)) }

    suspend fun login(correo: String, password: String): ApiResult<TokenResponse> {
        val resultado = ejecutarLlamada { api.login(correo, password) }
        if (resultado is ApiResult.Success) {
            tokenManager.guardarToken(resultado.data.access_token)
        }
        return resultado
    }

    suspend fun perfil(): ApiResult<UsuarioOut> = ejecutarLlamada { api.perfil() }

    suspend fun hayTokenGuardado(): Boolean = tokenManager.obtenerTokenSync() != null

    suspend fun cerrarSesion() = tokenManager.cerrarSesion()
}
