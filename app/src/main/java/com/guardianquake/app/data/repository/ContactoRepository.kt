package com.guardianquake.app.data.repository

import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.ContactoEmergenciaOut
import com.guardianquake.app.data.remote.dto.ContactoEmergenciaRequest

class ContactoRepository(private val api: ApiService) {

    suspend fun listar(): ApiResult<List<ContactoEmergenciaOut>> =
        ejecutarLlamada { api.listarContactos() }

    suspend fun crear(nombre: String, telefono: String): ApiResult<ContactoEmergenciaOut> =
        ejecutarLlamada { api.crearContacto(ContactoEmergenciaRequest(nombre, telefono)) }

    suspend fun eliminar(id: Int): ApiResult<Unit> =
        ejecutarLlamada { api.eliminarContacto(id) }
}
