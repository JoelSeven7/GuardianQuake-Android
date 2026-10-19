package com.guardianquake.app.ui.suscripcion

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
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
fun SuscripcionScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GuardianQuakeApp

    val viewModel: SuscripcionViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SuscripcionViewModel(app.suscripcionRepository) }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val esPro = uiState.tipoCuenta == "PRO"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Version Pro") },
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
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = if (esPro) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (esPro) "Ya tienes GuardianQuake Pro" else "Version gratuita",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                listOf(
                    "Notificacion automatica a tus contactos de emergencia",
                    "Historial de eventos sin limite",
                    "Calibracion avanzada por modelo de dispositivo"
                ).forEach { beneficio ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(beneficio, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (esPro) {
                    OutlinedButton(
                        onClick = { viewModel.cancelarPro() },
                        enabled = !uiState.procesando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar suscripcion")
                    }
                } else {
                    Button(
                        onClick = { viewModel.activarPro() },
                        enabled = !uiState.procesando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.procesando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Activar Pro (demo)")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "En produccion, este boton se conecta a Google Play Billing; " +
                            "aqui simula la compra para probar el flujo con el backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                uiState.mensaje?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
