package com.guardianquake.app.ui.sonido

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardianquake.app.GuardianQuakeApp
import com.guardianquake.app.ui.common.IndicadorCarga

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SonidoAlarmaScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GuardianQuakeApp

    val viewModel: SonidoAlarmaViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SonidoAlarmaViewModel(app.tokenManager, app) }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    val selectorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.seleccionarArchivo(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sonido de alarma") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cargando) {
            IndicadorCarga(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.nombreArchivo ?: "Sirena predeterminada de GuardianQuake",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Este es el sonido que se reproducira, en volumen de alarma, " +
                        "cuando la app detecte movimiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { selectorArchivo.launch(arrayOf("audio/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Elegir un sonido de mi telefono")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.restaurarPredeterminado() },
                    enabled = uiState.nombreArchivo != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar la sirena predeterminada")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (uiState.reproduciendoPrueba) viewModel.detenerPrueba() else viewModel.probarSonido()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (uiState.reproduciendoPrueba) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.reproduciendoPrueba) "Detener prueba" else "Probar sonido")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Si el archivo elegido deja de estar disponible (lo borras, " +
                        "cambias de telefono, etc.), la app usara la sirena predeterminada " +
                        "automaticamente, para que la alarma nunca se quede en silencio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
