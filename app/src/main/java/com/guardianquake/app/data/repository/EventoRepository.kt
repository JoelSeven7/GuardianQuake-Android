package com.guardianquake.app.data.repository

import com.guardianquake.app.data.local.EventoLocalDao
import com.guardianquake.app.data.local.EventoLocalEntity
import com.guardianquake.app.data.local.TokenManager
import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.dto.EventoSismicoOut
import com.guardianquake.app.data.remote.dto.EventoSismicoRequest
import kotlinx.coroutines.flow.Flow

class EventoRepository(
    private val api: ApiService,
    private val dao: EventoLocalDao,
    private val tokenManager: TokenManager
) {
    fun observarEventosLocales(): Flow<List<EventoLocalEntity>> = dao.observarTodos()

    /** Se llama cuando el detector local encuentra un posible sismo.
     *  Guarda primero en Room (siempre funciona, aun sin internet) y
     *  despues intenta respaldarlo en el backend (RF08). */
    suspend fun registrarEventoDetectado(
        fechaHoraIso: String,
        intensidad: Double,
        duracionMs: Int,
        sensibilidadUsada: Int
    ) {
        val idLocal = dao.insertar(
            EventoLocalEntity(
                fechaHora = fechaHoraIso,
                intensidadAproximada = intensidad,
                duracionMs = duracionMs,
                sensibilidadUsada = sensibilidadUsada,
                sincronizado = false
            )
        )

        val idDispositivo = tokenManager.obtenerIdDispositivoSync()
        val resultado = ejecutarLlamada {
            api.registrarEvento(
                EventoSismicoRequest(
                    id_dispositivo = idDispositivo,
                    fecha_hora = fechaHoraIso,
                    intensidad_aproximada = intensidad,
                    duracion_ms = duracionMs,
                    sensibilidad_usada = sensibilidadUsada
                )
            )
        }
        if (resultado is ApiResult.Success) {
            dao.actualizar(
                EventoLocalEntity(
                    id = idLocal,
                    fechaHora = fechaHoraIso,
                    intensidadAproximada = intensidad,
                    duracionMs = duracionMs,
                    sensibilidadUsada = sensibilidadUsada,
                    sincronizado = true
                )
            )
        }
        // Si falla (sin internet), el evento queda guardado localmente con
        // sincronizado = false; sincronizarPendientes() lo reintenta despues.
    }

    /** Reintenta subir al backend los eventos que quedaron pendientes por
     *  falta de conexion en su momento. Util al abrir la app o al detectar
     *  que volvio la conectividad. */
    suspend fun sincronizarPendientes() {
        val pendientes = dao.obtenerPendientesSincronizar()
        val idDispositivo = tokenManager.obtenerIdDispositivoSync()
        for (evento in pendientes) {
            val resultado = ejecutarLlamada {
                api.registrarEvento(
                    EventoSismicoRequest(
                        id_dispositivo = idDispositivo,
                        fecha_hora = evento.fechaHora,
                        intensidad_aproximada = evento.intensidadAproximada,
                        duracion_ms = evento.duracionMs,
                        sensibilidad_usada = evento.sensibilidadUsada
                    )
                )
            }
            if (resultado is ApiResult.Success) {
                dao.actualizar(evento.copy(sincronizado = true))
            }
        }
    }

    suspend fun listarDesdeBackend(): ApiResult<List<EventoSismicoOut>> =
        ejecutarLlamada { api.listarEventos() }
}
