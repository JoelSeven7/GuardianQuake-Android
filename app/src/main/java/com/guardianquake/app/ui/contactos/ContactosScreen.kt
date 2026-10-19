package com.guardianquake.app.ui.contactos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardianquake.app.GuardianQuakeApp
import com.guardianquake.app.ui.common.IndicadorCarga

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GuardianQuakeApp

    val viewModel: ContactosViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ContactosViewModel(app.contactoRepository) }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos de emergencia") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Telefono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.agregar(nombre.trim(), telefono.trim())
                    nombre = ""
                    telefono = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar contacto")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cargando) {
                IndicadorCarga()
            } else {
                LazyColumn {
                    items(uiState.contactos) { contacto ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text(contacto.telefono, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { viewModel.eliminar(contacto.id_contacto) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
