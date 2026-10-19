package com.guardianquake.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Deteccion 100% local (RNF01), diseñada para priorizar tiempo de
 * reaccion sobre eliminacion de falsos positivos (RF02, RNF03): la app
 * siempre opera con la sensibilidad MAXIMA, sin que el usuario pueda
 * bajarla. La responsabilidad de evitar falsas alarmas (elegir un
 * lugar aislado y estable) es del usuario, no del algoritmo.
 *
 * Dispara la alerta apenas el movimiento se CONFIRMA (una ventana
 * minima de [CONFIRMACION_MS], solo para descartar un pico de una
 * sola lectura de ruido del sensor, no para filtrar movimiento real).
 * El evento completo se registra en el historial una vez que el
 * movimiento cesa, pero la alarma ya sono desde antes.
 */
class SeismicDetector(
    context: Context,
    /** Se dispara UNA vez, tan pronto se confirma el movimiento (RNF03: <1s). */
    private val onAlertaInmediata: (intensidadActual: Double) -> Unit,
    /** Se dispara al terminar el movimiento, con el resumen completo para el historial (RF08). */
    private val onEventoFinalizado: (intensidadMaxima: Double, duracionMs: Long) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val acelerometro: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gravedad = FloatArray(3)
    private var filtroCalibrado = false
    private var enMovimiento = false
    private var yaAlerto = false
    private var inicioMovimiento = 0L
    private var intensidadMaxima = 0.0
    private var ultimaVezSobreUmbral = 0L

    companion object {
        private const val ALFA = 0.8f // constante del filtro paso bajo (aisla la gravedad)

        // Ventana minima para CONFIRMAR que es movimiento real y no un pico
        // de una sola lectura de ruido del sensor. 100ms es imperceptible
        // para una persona, pero suficiente para el sensor.
        private const val CONFIRMACION_MS = 100L

        // "Silencio" para dar por terminado el evento y cerrar el registro
        // en el historial (no afecta cuando suena la alarma).
        private const val VENTANA_FIN_MS = 800L

        /** Umbral fijo de aceleracion (m/s^2) por encima de la gravedad.
         *  Ya no es configurable: la app siempre usa el valor mas sensible
         *  posible, porque su proposito es alertar ante el primer indicio
         *  de movimiento, no filtrar movimientos cotidianos.
         *
         *  ADVERTENCIA TECNICA: el ruido propio del acelerometro (aun con
         *  el telefono perfectamente quieto) ronda ~0.03-0.05 m/s^2 en la
         *  mayoria de los dispositivos (confirmado en pruebas reales). Este
         *  umbral (0.1) queda apenas por encima de ese piso de ruido para
         *  no alarmar en reposo absoluto, pero reacciona ante practicamente
         *  cualquier vibracion perceptible. */
        const val UMBRAL_FIJO = 0.1
    }

    fun iniciar() {
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun detener() {
        sensorManager.unregisterListener(this)
        SensorLecturaBus.limpiar()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // CORRECCION DE BUG: si el filtro arranca en (0,0,0), tarda varias
        // lecturas en "subir" hasta la gravedad real (~9.8 m/s^2), y
        // mientras sube, la resta (total - gravedad) da un pico artificial
        // enorme que decae poco a poco (justo lo que se veia como "ruido
        // alto al inicio"). Se soluciona inicializando la gravedad con la
        // primera lectura real, para que el filtro arranque ya calibrado.
        if (!filtroCalibrado) {
            gravedad[0] = event.values[0]
            gravedad[1] = event.values[1]
            gravedad[2] = event.values[2]
            filtroCalibrado = true
            return
        }

        // Filtro paso bajo: aisla la componente de gravedad
        gravedad[0] = ALFA * gravedad[0] + (1 - ALFA) * event.values[0]
        gravedad[1] = ALFA * gravedad[1] + (1 - ALFA) * event.values[1]
        gravedad[2] = ALFA * gravedad[2] + (1 - ALFA) * event.values[2]

        // Aceleracion lineal = aceleracion total - gravedad
        val x = event.values[0] - gravedad[0]
        val y = event.values[1] - gravedad[1]
        val z = event.values[2] - gravedad[2]

        val magnitud = sqrt((x * x + y * y + z * z).toDouble())
        val ahora = System.currentTimeMillis()

        // RF06: publicar cada lectura para la grafica en vivo (sismograma).
        SensorLecturaBus.publicar(magnitud.toFloat())

        if (magnitud > UMBRAL_FIJO) {
            ultimaVezSobreUmbral = ahora
            if (!enMovimiento) {
                enMovimiento = true
                inicioMovimiento = ahora
                intensidadMaxima = magnitud
                yaAlerto = false
            } else if (magnitud > intensidadMaxima) {
                intensidadMaxima = magnitud
            }

            // Disparar la alarma INMEDIATAMENTE al confirmar (no esperar
            // a que el movimiento termine). Solo una vez por evento.
            if (!yaAlerto && (ahora - inicioMovimiento) >= CONFIRMACION_MS) {
                yaAlerto = true
                onAlertaInmediata(intensidadMaxima)
            }
        } else if (enMovimiento && (ahora - ultimaVezSobreUmbral) > VENTANA_FIN_MS) {
            // El movimiento se detuvo: cerrar el registro para el historial,
            // solo si en efecto llego a confirmarse.
            val duracion = ahora - inicioMovimiento
            if (yaAlerto) {
                onEventoFinalizado(intensidadMaxima, duracion)
            }
            enMovimiento = false
            intensidadMaxima = 0.0
            yaAlerto = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se necesita accion; requerido por la interfaz.
    }
}
