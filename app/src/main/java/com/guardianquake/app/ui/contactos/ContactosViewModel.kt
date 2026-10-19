package com.guardianquake.app.ui.contactos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.remote.dto.ContactoEmergenciaOut
import com.guardianquake.app.data.repository.ApiResult
import com.guardianquake.app.data.repository.ContactoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactosUiState(
    val contactos: List<ContactoEmergenciaOut> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class ContactosViewModel(
    private val contactoRepository: ContactoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactosUiState())
    val uiState: StateFlow<ContactosUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            when (val resultado = contactoRepository.listar()) {
                is ApiResult.Success -> _uiState.value = ContactosUiState(contactos = resultado.data, cargando = false)
                is ApiResult.Error -> _uiState.value = ContactosUiState(cargando = false, error = resultado.mensaje)
            }
        }
    }

    fun agregar(nombre: String, telefono: String) {
        if (nombre.isBlank() || telefono.isBlank()) return
        viewModelScope.launch {
            when (contactoRepository.crear(nombre, telefono)) {
                is ApiResult.Success -> cargar()
                is ApiResult.Error -> { /* el mensaje ya se muestra en la UI si se necesita */ }
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            when (contactoRepository.eliminar(id)) {
                is ApiResult.Success -> cargar()
                is ApiResult.Error -> { /* idem */ }
            }
        }
    }
}
