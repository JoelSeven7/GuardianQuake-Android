package com.guardianquake.app.ui.dashboard

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardianquake.app.sensor.AlarmaBus
import com.guardianquake.app.sensor.MonitoringService
import com.guardianquake.app.ui.common.SismogramaEnVivo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onIrAHistorial: () -> Unit,
    onIrASonidoAlarma: () -> Unit,
    onIrAContenido: () -> Unit,
    onIrASuscripcion: () -> Unit,
    onIrAContactos: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DashboardViewModel() }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    val alarmaSonando by AlarmaBus.sonando.collectAsState()

    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* si se niega, el monitoreo sigue funcionando, solo sin notificaciones visibles */ }

    LaunchedEffect(Unit) {
        viewModel.actualizarEstadoMonitoreo(MonitoringService.monitoreoActivo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun iniciarServicioMonitoreo() {
        val intent = Intent(context, MonitoringService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun alternarMonitoreo(activar: Boolean) {
        if (activar) {
            // El switch se ve activado de inmediato (feedback visual), pero
            // el servicio real arranca solo despues de la cuenta regresiva,
            // para darle tiempo al usuario de soltar el telefono sin que
            // ese mismo gesto dispare una alerta con la sensibilidad tan alta.
            viewModel.actualizarEstadoMonitoreo(true)
            viewModel.iniciarCuentaRegresiva(5) { iniciarServicioMonitoreo() }
        } else {
            viewModel.cancelarCuentaRegresiva() // por si se arrepintio a mitad de la cuenta
            val intent = Intent(context, MonitoringService::class.java)
            context.stopService(intent)
            viewModel.actualizarEstadoMonitoreo(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GuardianQuake") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (alarmaSonando) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡ALARMA SONANDO!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Text(
                            text = "Verifica la situacion antes de continuar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { MonitoringService.detenerAlarmaActual() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onError,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DETENER ALARMA")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.monitoreoActivo)
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (uiState.monitoreoActivo) Icons.Default.Shield else Icons.Default.GppMaybe,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (uiState.monitoreoActivo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when {
                            uiState.enCuentaRegresiva -> "Preparando monitoreo..."
                            uiState.monitoreoActivo -> "Monitoreo activo"
                            else -> "Monitoreo desactivado"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Sensibilidad: siempre al maximo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Switch(
                        checked = uiState.monitoreoActivo,
                        onCheckedChange = { activar -> alternarMonitoreo(activar) }
                    )

                    if (uiState.enCuentaRegresiva) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "${uiState.segundosRestantes}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Suelta el telefono en su lugar...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (uiState.monitoreoActivo) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SismogramaEnVivo(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Este monitoreo funciona 100% en tu dispositivo, incluso sin conexion a internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Coloca el telefono en una superficie estable y aislada, donde " +
                            "nadie lo pueda mover ni tropezar, y sube el volumen al maximo. " +
                            "La alarma debe escucharse fuerte para darte tiempo de reaccionar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OpcionMenu(icono = Icons.Default.History, texto = "Historial de eventos", onClick = onIrAHistorial)
            OpcionMenu(icono = Icons.Default.MusicNote, texto = "Sonido de alarma", onClick = onIrASonidoAlarma)
            OpcionMenu(icono = Icons.Default.School, texto = "Contenido educativo", onClick = onIrAContenido)
            OpcionMenu(icono = Icons.Default.WorkspacePremium, texto = "Version Pro", onClick = onIrASuscripcion)
            OpcionMenu(icono = Icons.Default.ContactPhone, texto = "Contactos de emergencia", onClick = onIrAContactos)
        }
    }
}

@Composable
private fun OpcionMenu(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icono, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
