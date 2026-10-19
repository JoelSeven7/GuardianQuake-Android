package com.guardianquake.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.repository.ApiResult
import com.guardianquake.app.data.repository.AuthRepository
import com.guardianquake.app.data.repository.DispositivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val autenticado: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dispositivoRepository: DispositivoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun registrar(correo: String, password: String, onExito: () -> Unit) {
        if (correo.isBlank() || password.length < 6) {
            _uiState.value = AuthUiState(error = "Correo requerido y contraseña de al menos 6 caracteres")
            return
        }
        _uiState.value = AuthUiState(cargando = true)
        viewModelScope.launch {
            when (val resultado = authRepository.registro(correo, password)) {
                is ApiResult.Success -> {
                    _uiState.value = AuthUiState()
                    onExito()
                }
                is ApiResult.Error -> {
                    _uiState.value = AuthUiState(error = resultado.mensaje)
                }
            }
        }
    }

    fun login(correo: String, password: String, onExito: () -> Unit) {
        if (correo.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Completa correo y contraseña")
            return
        }
        _uiState.value = AuthUiState(cargando = true)
        viewModelScope.launch {
            when (val resultado = authRepository.login(correo, password)) {
                is ApiResult.Success -> {
                    // Registrar el dispositivo (RNF04) justo despues del primer login
                    dispositivoRepository.asegurarDispositivoRegistrado()
                    _uiState.value = AuthUiState(autenticado = true)
                    onExito()
                }
                is ApiResult.Error -> {
                    _uiState.value = AuthUiState(error = resultado.mensaje)
                }
            }
        }
    }
}
