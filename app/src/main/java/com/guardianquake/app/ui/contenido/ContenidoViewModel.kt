package com.guardianquake.app.ui.contenido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.remote.dto.ContenidoEducativoOut
import com.guardianquake.app.data.repository.ApiResult
import com.guardianquake.app.data.repository.ContenidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContenidoListaUiState(
    val items: List<ContenidoEducativoOut> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class ContenidoListaViewModel(
    private val contenidoRepository: ContenidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContenidoListaUiState())
    val uiState: StateFlow<ContenidoListaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val resultado = contenidoRepository.listar()) {
                is ApiResult.Success -> _uiState.value = ContenidoListaUiState(items = resultado.data, cargando = false)
                is ApiResult.Error -> _uiState.value = ContenidoListaUiState(cargando = false, error = resultado.mensaje)
            }
        }
    }
}

data class ContenidoDetalleUiState(
    val contenido: ContenidoEducativoOut? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

class ContenidoDetalleViewModel(
    private val contenidoRepository: ContenidoRepository,
    idContenido: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContenidoDetalleUiState())
    val uiState: StateFlow<ContenidoDetalleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val resultado = contenidoRepository.obtener(idContenido)) {
                is ApiResult.Success -> _uiState.value = ContenidoDetalleUiState(contenido = resultado.data, cargando = false)
                is ApiResult.Error -> _uiState.value = ContenidoDetalleUiState(cargando = false, error = resultado.mensaje)
            }
        }
    }
}
