package com.guardianquake.app.sensor

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.guardianquake.app.GuardianQuakeApp
import com.guardianquake.app.MainActivity
import com.guardianquake.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Servicio en primer plano (RF01/RF02): mientras esta activo, el
 * telefono vigila movimiento de forma continua usando el acelerometro,
 * sin necesitar la app abierta en pantalla ni conexion a internet.
 * Mantiene un WakeLock parcial mientras el monitoreo esta activo, para
 * que el CPU no entre en reposo profundo con la pantalla apagada -- sin
 * esto, las lecturas del sensor se pausan hasta que algo despierta el
 * telefono (como encender la pantalla), justo cuando mas se necesita
 * deteccion en tiempo real. Esto tiene un costo real de bateria: es el
 * trade-off explicito de dejar el telefono vigilando 24/7.
 *
 * RNF03/RNF09: la alarma suena INMEDIATAMENTE al confirmarse movimiento
 * (no espera a que termine), usando el canal de audio de ALARMA del
 * sistema (no notificaciones), para que se escuche aunque el telefono
 * este en silencio o con el volumen de notificaciones bajo. Suena de
 * forma INDEFINIDA -- no se apaga sola con un temporizador -- hasta que
 * el usuario la detiene manualmente desde la app, porque el objetivo es
 * no perder la alerta aunque la persona tarde en reaccionar.
 */
class MonitoringService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var detector: SeismicDetector? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var uriSonidoPersonalizado: String? = null

    companion object {
        const val ID_NOTIFICACION_SERVICIO = 1001
        const val ID_NOTIFICACION_ALERTA = 1002

        var monitoreoActivo = false
            private set

        private var instanciaActual: MonitoringService? = null

        /** Se llama desde la UI (boton "Detener alarma" en el Dashboard)
         *  cuando el usuario confirma que ya esta a salvo. */
        fun detenerAlarmaActual() {
            instanciaActual?.detenerAlarmaSonora()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instanciaActual = this

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "GuardianQuake::MonitoreoWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        vibrator = obtenerVibrator()
        val app = applicationContext as GuardianQuakeApp
        uriSonidoPersonalizado = runBlocking { app.tokenManager.obtenerSonidoAlarmaUriSync() }

        startForeground(ID_NOTIFICACION_SERVICIO, crearNotificacionServicio())
        monitoreoActivo = true

        detector = SeismicDetector(
            context = applicationContext,
            onAlertaInmediata = { intensidad -> onAlertaInmediata(intensidad) },
            onEventoFinalizado = { intensidadMaxima, duracion -> onEventoFinalizado(intensidadMaxima, duracion) }
        ).also {
            it.iniciar()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        detenerAlarmaSonora()
        detector?.detener()
        monitoreoActivo = false
        instanciaActual = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** RNF03/RNF09: se llama apenas se CONFIRMA movimiento, sin esperar a que termine. */
    private fun onAlertaInmediata(intensidadActual: Double) {
        mostrarNotificacionAlerta(intensidadActual)
        dispararAlarmaSonora()
    }

    /** RF08: se llama cuando el movimiento ya terminó, para guardar el resumen en el historial. */
    private fun onEventoFinalizado(intensidadMaxima: Double, duracionMs: Long) {
        val app = applicationContext as GuardianQuakeApp
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val fechaHoraIso = formato.format(Date())

        scope.launch {
            app.eventoRepository.registrarEventoDetectado(
                fechaHoraIso = fechaHoraIso,
                intensidad = intensidadMaxima,
                duracionMs = duracionMs.toInt(),
                sensibilidadUsada = 10
            )
        }
    }

    /** RNF09: reproduce el sonido de alarma del sistema por el canal STREAM_ALARM
     *  (ignora el modo silencio/vibrar y el volumen de notificaciones), y vibra
     *  de forma sostenida, para maximizar la probabilidad de que se escuche.
     *
     *  Si el movimiento sigue en rafagas (nuevas confirmaciones mientras la
     *  alarma ya esta sonando), NO se reinicia el audio ni la vibracion --
     *  siguen su curso sin interrupcion. Solo el boton "Detener alarma" del
     *  Dashboard debe detenerlos. */
    private fun dispararAlarmaSonora() {
        if (mediaPlayer != null) {
            return // ya esta sonando por un evento anterior; se deja seguir
        }
        AlarmaBus.marcarSonando(true)

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        val atributosAlarma = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // RF05: si el usuario eligio un sonido propio, se usa ese. Si no
        // eligio ninguno, o el elegido ya no es accesible (lo borro, cambio
        // de telefono, revoco el permiso), se cae al sonido predeterminado
        // empaquetado en la app -- la alarma NUNCA debe quedarse muda.
        val sonoConExito = uriSonidoPersonalizado?.let { uriGuardada ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(atributosAlarma)
                    setDataSource(this@MonitoringService, Uri.parse(uriGuardada))
                    isLooping = true
                    prepare()
                    start()
                }
                true
            } catch (e: Exception) {
                false
            }
        } ?: false

        if (!sonoConExito) {
            try {
                mediaPlayer = MediaPlayer.create(this, R.raw.alarma, atributosAlarma, 0)?.apply {
                    isLooping = true
                    start()
                }
            } catch (e: Exception) {
                // Si ni siquiera el sonido predeterminado pudo reproducirse, al
                // menos que la vibracion y la notificacion sigan funcionando.
            }
        }

        vibrator?.let { vib ->
            val patron = longArrayOf(0, 800, 400, 800, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(patron, 0)) // repetir desde el indice 0
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(patron, 0)
            }
        }

        // NOTA: la alarma ya NO se apaga sola con un temporizador. Suena
        // indefinidamente hasta que el usuario la detiene manualmente desde
        // la app (boton "Detener alarma" en el Dashboard) o desactiva el
        // monitoreo -- el objetivo es no perder la alerta aunque la persona
        // tarde en reaccionar.
    }

    private fun detenerAlarmaSonora() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {
            }
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        AlarmaBus.marcarSonando(false)
    }

    private fun obtenerVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun crearNotificacionServicio(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, GuardianQuakeApp.CANAL_MONITOREO_ID)
            .setContentTitle("GuardianQuake activo")
            .setContentText("Vigilando movimiento en segundo plano")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun mostrarNotificacionAlerta(intensidad: Double) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, GuardianQuakeApp.CANAL_ALERTAS_ID)
            .setContentTitle("MOVIMIENTO FUERTE DETECTADO")
            .setContentText("Verifica de inmediato si es un sismo. Toca para abrir la app.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(ID_NOTIFICACION_ALERTA, notificacion)
    }
}
