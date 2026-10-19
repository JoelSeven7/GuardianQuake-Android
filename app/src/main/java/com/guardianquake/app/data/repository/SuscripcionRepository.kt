package com.guardianquake.app.data.repository

import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.EstadoSuscripcionOut
import com.guardianquake.app.data.remote.dto.SuscripcionOut
import com.guardianquake.app.data.remote.dto.VerificarCompraRequest

class SuscripcionRepository(private val api: ApiService) {

    suspend fun obtenerEstado(): ApiResult<EstadoSuscripcionOut> =
        ejecutarLlamada { api.estadoSuscripcion() }

    suspend fun verificarCompra(idTransaccion: String): ApiResult<SuscripcionOut> =
        ejecutarLlamada { api.verificarCompra(VerificarCompraRequest(idTransaccion)) }

    suspend fun cancelar(): ApiResult<SuscripcionOut> =
        ejecutarLlamada { api.cancelarSuscripcion() }
}
