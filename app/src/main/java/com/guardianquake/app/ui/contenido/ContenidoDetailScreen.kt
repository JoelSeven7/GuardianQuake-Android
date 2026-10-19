package com.guardianquake.app.ui.contenido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardianquake.app.GuardianQuakeApp
import com.guardianquake.app.ui.common.IndicadorCarga
import com.guardianquake.app.ui.common.MensajeError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoDetalleScreen(idContenido: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GuardianQuakeApp

    val viewModel: ContenidoDetalleViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ContenidoDetalleViewModel(app.contenidoRepository, idContenido) }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.contenido?.titulo ?: "Contenido") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.cargando -> IndicadorCarga(modifier = Modifier.padding(padding))
            uiState.error != null -> MensajeError(uiState.error ?: "Error desconocido", modifier = Modifier.padding(padding))
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(uiState.contenido?.cuerpo ?: "", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Version ${uiState.contenido?.version}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
