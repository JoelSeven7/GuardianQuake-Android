package com.guardianquake.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.guardianquake.app.data.local.AppDatabase
import com.guardianquake.app.data.local.TokenManager
import com.guardianquake.app.data.remote.ApiService
import com.guardianquake.app.data.remote.RetrofitClient
import com.guardianquake.app.data.repository.AuthRepository
import com.guardianquake.app.data.repository.ContactoRepository
import com.guardianquake.app.data.repository.ContenidoRepository
import com.guardianquake.app.data.repository.DispositivoRepository
import com.guardianquake.app.data.repository.EventoRepository
import com.guardianquake.app.data.repository.SuscripcionRepository

/**
 * En vez de un framework de inyeccion de dependencias (Hilt/Koin), este
 * proyecto usa un localizador de servicios simple para minimizar
 * dependencias adicionales. Para un proyecto en crecimiento, Hilt seria
 * la siguiente evolucion natural.
 */
class GuardianQuakeApp : Application() {

    lateinit var tokenManager: TokenManager
        private set
    lateinit var apiService: ApiService
        private set
    lateinit var database: AppDatabase
        private set

    lateinit var authRepository: AuthRepository
        private set
    lateinit var dispositivoRepository: DispositivoRepository
        private set
    lateinit var eventoRepository: EventoRepository
        private set
    lateinit var contenidoRepository: ContenidoRepository
        private set
    lateinit var suscripcionRepository: SuscripcionRepository
        private set
    lateinit var contactoRepository: ContactoRepository
        private set

    companion object {
        const val CANAL_MONITOREO_ID = "guardianquake_monitoreo"
        const val CANAL_ALERTAS_ID = "guardianquake_alertas"
    }

    override fun onCreate() {
        super.onCreate()

        tokenManager = TokenManager(this)
        apiService = RetrofitClient.getInstance(tokenManager)
        database = AppDatabase.getInstance(this)

        authRepository = AuthRepository(apiService, tokenManager)
        dispositivoRepository = DispositivoRepository(apiService, tokenManager)
        eventoRepository = EventoRepository(apiService, database.eventoLocalDao(), tokenManager)
        contenidoRepository = ContenidoRepository(apiService)
        suscripcionRepository = SuscripcionRepository(apiService)
        contactoRepository = ContactoRepository(apiService)

        crearCanalesNotificacion()
    }

    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val canalMonitoreo = NotificationChannel(
                CANAL_MONITOREO_ID,
                "Monitoreo activo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Indica que GuardianQuake esta vigilando movimiento en segundo plano"
            }

            val canalAlertas = NotificationChannel(
                CANAL_ALERTAS_ID,
                "Alertas de movimiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Se dispara cuando se detecta un posible sismo"
            }

            manager.createNotificationChannel(canalMonitoreo)
            manager.createNotificationChannel(canalAlertas)
        }
    }
}
