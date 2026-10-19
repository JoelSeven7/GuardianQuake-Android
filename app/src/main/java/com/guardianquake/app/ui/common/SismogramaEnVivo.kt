package com.guardianquake.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guardianquake.app.sensor.SeismicDetector
import com.guardianquake.app.sensor.SensorLecturaBus

/**
 * RF06: sismograma en vivo, compacto, pensado para vivir dentro del
 * Dashboard (debajo del interruptor de monitoreo), no como pantalla
 * separada. Solo observa lo que el MonitoringService ya esta
 * publicando en [SensorLecturaBus] -- no abre un segundo listener del
 * sensor.
 */
@Composable
fun SismogramaEnVivo(
    modifier: Modifier = Modifier,
    alturaGrafica: Dp = 130.dp
) {
    val historial by SensorLecturaBus.historial.collectAsState()
    val umbral = SeismicDetector.UMBRAL_FIJO.toFloat()
    val magnitudActual = historial.lastOrNull() ?: 0f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "%.2f m/s²".format(magnitudActual),
                style = MaterialTheme.typography.titleMedium,
                color = if (magnitudActual > umbral)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
            Text(
                text = "● En vivo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(alturaGrafica)
        ) {
            val ancho = size.width
            val alto = size.height
            // Escala visual mucho mas ajustada que antes: con el umbral en
            // 0.3, un tope de 4 da buen margen para ver picos sin que la
            // linea quede pegada al fondo del grafico.
            val escalaMax = 4f

            val yUmbral = alto - (umbral / escalaMax * alto).coerceIn(0f, alto)
            drawLine(
                color = Color.Red.copy(alpha = 0.6f),
                start = Offset(0f, yUmbral),
                end = Offset(ancho, yUmbral),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
            )

            if (historial.size >= 2) {
                val pasoX = ancho / (historial.size - 1).coerceAtLeast(1)
                val path = Path()
                historial.forEachIndexed { i, valor ->
                    val x = i * pasoX
                    val y = alto - (valor / escalaMax * alto).coerceIn(0f, alto)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = Color(0xFF2E7D32), style = Stroke(width = 4f))
            }
        }
    }
}
