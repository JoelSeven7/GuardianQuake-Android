package com.guardianquake.app.ui.sonido

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianquake.app.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SonidoAlarmaUiState(
    val nombreArchivo: String? = null, // null = usando la sirena predeterminada
    val reproduciendoPrueba: Boolean = false,
    val cargando: Boolean = true
)

/**
 * RF05: permite al usuario elegir un archivo de audio propio (de su
 * telefono) para usar como sonido de alarma, en vez de la sirena
 * predeterminada. Se guarda localmente (funciona sin internet, RNF01).
 * Si el archivo elegido deja de estar disponible, MonitoringService cae
 * de vuelta a la sirena predeterminada automaticamente -- nunca se
 * queda sin sonido.
 */
class SonidoAlarmaViewModel(
    private val tokenManager: TokenManager,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SonidoAlarmaUiState())
    val uiState: StateFlow<SonidoAlarmaUiState> = _uiState.asStateFlow()

    private var mediaPlayerPrueba: MediaPlayer? = null

    init {
        viewModelScope.launch {
            val uriGuardada = tokenManager.obtenerSonidoAlarmaUriSync()
            _uiState.value = SonidoAlarmaUiState(
                nombreArchivo = uriGuardada?.let { obtenerNombreArchivo(Uri.parse(it)) },
                cargando = false
            )
        }
    }

    fun seleccionarArchivo(uri: Uri) {
        // Permiso persistente: sin esto, el acceso al archivo se pierde
        // al reiniciar el telefono o cerrar la app.
        try {
            appContext.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Algunos proveedores de archivos no soportan permiso persistente;
            // igual guardamos la uri, MonitoringService cae al respaldo si falla.
        }

        viewModelScope.launch {
            tokenManager.guardarSonidoAlarmaUri(uri.toString())
            _uiState.value = _uiState.value.copy(nombreArchivo = obtenerNombreArchivo(uri))
        }
    }

    fun restaurarPredeterminado() {
        detenerPrueba()
        viewModelScope.launch {
            tokenManager.guardarSonidoAlarmaUri(null)
            _uiState.value = _uiState.value.copy(nombreArchivo = null)
        }
    }

    fun probarSonido() {
        viewModelScope.launch {
            detenerPrueba()
            val uriGuardada = tokenManager.obtenerSonidoAlarmaUriSync()
            try {
                mediaPlayerPrueba = if (uriGuardada != null) {
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setDataSource(appContext, Uri.parse(uriGuardada))
                        prepare()
                        start()
                    }
                } else {
                    MediaPlayer.create(
                        appContext,
                        com.guardianquake.app.R.raw.alarma,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                        0
                    )?.apply { start() }
                }
                mediaPlayerPrueba?.setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(reproduciendoPrueba = false)
                }
                _uiState.value = _uiState.value.copy(reproduciendoPrueba = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(reproduciendoPrueba = false)
            }
        }
    }

    fun detenerPrueba() {
        mediaPlayerPrueba?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {
            }
            release()
        }
        mediaPlayerPrueba = null
        _uiState.value = _uiState.value.copy(reproduciendoPrueba = false)
    }

    override fun onCleared() {
        detenerPrueba()
        super.onCleared()
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "Archivo de audio"
        try {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (indice >= 0 && cursor.moveToFirst()) {
                    nombre = cursor.getString(indice)
                }
            }
        } catch (_: Exception) {
            // Si no se puede leer el nombre, se muestra el generico.
        }
        return nombre
    }
}
