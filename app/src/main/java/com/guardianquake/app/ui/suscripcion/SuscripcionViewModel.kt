package com.guardianquake.app.ui.suscripcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.repository.ApiResult
import com.guardianquake.app.data.repository.SuscripcionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class SuscripcionUiState(
    val tipoCuenta: String = "FREE",
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val mensaje: String? = null
)

class SuscripcionViewModel(
    private val suscripcionRepository: SuscripcionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuscripcionUiState())
    val uiState: StateFlow<SuscripcionUiState> = _uiState.asStateFlow()

    init {
        cargarEstado()
    }

    private fun cargarEstado() {
        viewModelScope.launch {
            when (val resultado = suscripcionRepository.obtenerEstado()) {
                is ApiResult.Success -> _uiState.value = SuscripcionUiState(
                    tipoCuenta = resultado.data.tipo_cuenta,
                    cargando = false
                )
                is ApiResult.Error -> _uiState.value = SuscripcionUiState(cargando = false, mensaje = resultado.mensaje)
            }
        }
    }

    /** En un flujo real, este id_transaccion vendria de la Google Play Billing
     *  Library tras completar la compra. Aqui se simula para fines de demo. */
    fun activarPro() {
        _uiState.value = _uiState.value.copy(procesando = true, mensaje = null)
        viewModelScope.launch {
            val idTransaccionSimulado = "DEMO-${UUID.randomUUID()}"
            when (val resultado = suscripcionRepository.verificarCompra(idTransaccionSimulado)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        tipoCuenta = "PRO",
                        procesando = false,
                        mensaje = "Version Pro activada"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    procesando = false,
                    mensaje = resultado.mensaje
                )
            }
        }
    }

    fun cancelarPro() {
        _uiState.value = _uiState.value.copy(procesando = true, mensaje = null)
        viewModelScope.launch {
            when (val resultado = suscripcionRepository.cancelar()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    tipoCuenta = "FREE",
                    procesando = false,
                    mensaje = "Suscripcion cancelada"
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    procesando = false,
                    mensaje = resultado.mensaje
                )
            }
        }
    }
}
