package com.guardianquake.app.data.repository

import android.os.Build
import com.guardianquake.app.data.local.TokenManager
import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.CalibracionOut
import com.guardianquake.app.data.remote.dto.DispositivoCrearRequest
import com.guardianquake.app.data.remote.dto.DispositivoOut

class DispositivoRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    /** Registra este telefono en el backend la primera vez que se necesite, y
     *  reutiliza el id guardado en las siguientes ejecuciones. */
    suspend fun asegurarDispositivoRegistrado(): ApiResult<Int> {
        val idExistente = tokenManager.obtenerIdDispositivoSync()
        if (idExistente != null) return ApiResult.Success(idExistente)

        val request = DispositivoCrearRequest(
            modelo = Build.MODEL ?: "Desconocido",
            fabricante = Build.MANUFACTURER ?: "Desconocido",
            so_version = "Android ${Build.VERSION.RELEASE}"
        )
        val resultado = ejecutarLlamada { api.registrarDispositivo(request) }
        return when (resultado) {
            is ApiResult.Success -> {
                tokenManager.guardarIdDispositivo(resultado.data.id_dispositivo)
                ApiResult.Success(resultado.data.id_dispositivo)
            }
            is ApiResult.Error -> resultado
        }
    }

    suspend fun obtenerCalibracion(idDispositivo: Int): ApiResult<CalibracionOut> =
        ejecutarLlamada { api.obtenerCalibracion(idDispositivo) }
}
