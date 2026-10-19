package com.guardianquake.app.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Puente en memoria entre el SeismicDetector (que corre dentro del
 * MonitoringService) y la pantalla de monitor en tiempo real (RF06).
 * No usa Room ni red: es solo un buffer volatil para graficar lo que
 * el sensor esta midiendo en este instante. Al no haber monitoreo
 * activo, simplemente no llegan lecturas nuevas.
 */
object SensorLecturaBus {

    private const val MAX_PUNTOS = 150

    private val _historial = MutableStateFlow<List<Float>>(emptyList())
    val historial: StateFlow<List<Float>> = _historial.asStateFlow()

    private val _umbralActual = MutableStateFlow(SeismicDetector.UMBRAL_FIJO.toFloat())
    val umbralActual: StateFlow<Float> = _umbralActual.asStateFlow()

    fun publicar(magnitud: Float) {
        _historial.value = (_historial.value + magnitud).takeLast(MAX_PUNTOS)
    }

    fun limpiar() {
        _historial.value = emptyList()
    }
}
