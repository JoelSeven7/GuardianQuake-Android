package com.guardianquake.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val monitoreoActivo: Boolean = false,
    val enCuentaRegresiva: Boolean = false,
    val segundosRestantes: Int = 0
)

/**
 * La sensibilidad ya no es configurable (RF06 ahora es el sismograma en
 * vivo): la app siempre opera al maximo de sensibilidad. Este ViewModel
 * ademas maneja la cuenta regresiva antes de arrancar el monitoreo, para
 * darle tiempo al usuario de soltar el telefono en el lugar donde va a
 * quedar vigilando (si arrancara de inmediato, el propio gesto de dejarlo
 * en la mesa podria disparar una alerta con la sensibilidad tan alta).
 */
class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var jobCuentaRegresiva: Job? = null

    fun actualizarEstadoMonitoreo(activo: Boolean) {
        _uiState.value = _uiState.value.copy(monitoreoActivo = activo)
    }

    /** Cuenta hacia atras desde [segundos] y solo entonces ejecuta [onFinalizar]
     *  (que es quien de verdad arranca el MonitoringService). */
    fun iniciarCuentaRegresiva(segundos: Int = 5, onFinalizar: () -> Unit) {
        jobCuentaRegresiva?.cancel()
        jobCuentaRegresiva = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enCuentaRegresiva = true, segundosRestantes = segundos)
            for (s in segundos downTo 1) {
                _uiState.value = _uiState.value.copy(segundosRestantes = s)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(enCuentaRegresiva = false)
            onFinalizar()
        }
    }

    /** Se llama si el usuario apaga el switch mientras aun esta contando
     *  (se arrepintio antes de que el servicio siquiera arrancara). */
    fun cancelarCuentaRegresiva() {
        jobCuentaRegresiva?.cancel()
        jobCuentaRegresiva = null
        _uiState.value = _uiState.value.copy(enCuentaRegresiva = false)
    }
}
