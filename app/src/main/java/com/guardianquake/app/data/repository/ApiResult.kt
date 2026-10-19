package com.guardianquake.app.data.repository

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val mensaje: String, val codigo: Int? = null) : ApiResult<Nothing>()
}

suspend fun <T> ejecutarLlamada(llamada: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = llamada()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }
        } else {
            val mensaje = response.errorBody()?.string() ?: "Error ${response.code()}"
            ApiResult.Error(mensaje, response.code())
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Error de conexion. Verifica tu internet o el servidor.")
    }
}
