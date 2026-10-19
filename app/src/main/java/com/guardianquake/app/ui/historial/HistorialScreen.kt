package com.guardianquake.app.ui.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
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
import com.guardianquake.app.data.local.EventoLocalEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GuardianQuakeApp

    val viewModel: HistorialViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HistorialViewModel(app.eventoRepository) }
        }
    )
    val eventos by viewModel.eventos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de eventos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (eventos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aun no se ha detectado ningun movimiento")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(eventos) { evento ->
                    TarjetaEvento(evento)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvento(evento: EventoLocalEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = evento.fechaHora, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Intensidad: ${"%.2f".format(evento.intensidadAproximada)} m/s² · " +
                        "Duracion: ${evento.duracionMs / 1000.0}s",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Sensibilidad usada: ${evento.sensibilidadUsada}/10",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = if (evento.sincronizado) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                contentDescription = if (evento.sincronizado) "Respaldado en la nube" else "Pendiente de respaldo",
                tint = if (evento.sincronizado)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}
