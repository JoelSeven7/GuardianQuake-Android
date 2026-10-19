package com.guardianquake.app.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Puente en memoria entre MonitoringService (que reproduce la alarma) y
 * el Dashboard (que necesita saber si esta sonando para mostrar el
 * boton de "Detener alarma"). La alarma ya NO se apaga sola: suena
 * indefinidamente hasta que el usuario la detiene desde la app, porque
 * el objetivo es no perder la alerta aunque tarde en reaccionar.
 */
object AlarmaBus {
    private val _sonando = MutableStateFlow(false)
    val sonando: StateFlow<Boolean> = _sonando.asStateFlow()

    fun marcarSonando(valor: Boolean) {
        _sonando.value = valor
    }
}
