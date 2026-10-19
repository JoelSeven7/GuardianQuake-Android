package com.guardianquake.app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.local.EventoLocalEntity
import com.guardianquake.app.data.repository.EventoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val eventoRepository: EventoRepository
) : ViewModel() {

    val eventos: StateFlow<List<EventoLocalEntity>> = eventoRepository.observarEventosLocales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Intenta respaldar en el backend cualquier evento que haya quedado
        // pendiente por falta de conexion en su momento (RF08).
        viewModelScope.launch {
            eventoRepository.sincronizarPendientes()
        }
    }
}
