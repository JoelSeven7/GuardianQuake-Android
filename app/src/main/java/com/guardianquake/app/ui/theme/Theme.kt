package com.guardianquake.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    secondary = NaranjaAlerta,
    tertiary = VerdeSeguro,
    background = GrisClaro,
    error = RojoError
)

private val EsquemaOscuro = darkColorScheme(
    primary = NaranjaAlerta,
    secondary = AzulProfundo,
    tertiary = VerdeSeguro,
    error = RojoError
)

@Composable
fun GuardianQuakeTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) EsquemaOscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
