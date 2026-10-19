package com.guardianquake.app.data.repository

import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.ContenidoEducativoOut

class ContenidoRepository(private val api: ApiService) {

    suspend fun listar(): ApiResult<List<ContenidoEducativoOut>> =
        ejecutarLlamada { api.listarContenido() }

    suspend fun obtener(id: Int): ApiResult<ContenidoEducativoOut> =
        ejecutarLlamada { api.obtenerContenido(id) }
}
