package app.flameproject.weather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import app.flameproject.weather.domain.model.WeatherCondition
import app.flameproject.weather.ui.theme.WeatherTheme
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private class RainParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var length: Float = 0f,
    var speed: Float = 0f,
    var alpha: Float = 0f
)

private class SnowParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 0f,
    var speed: Float = 0f,
    var sway: Float = 0f,
    var phase: Float = 0f
)

/**
 * Native hardware-accelerated weather particle effects running at device refresh rate.
 */
@Composable
fun AtmosphericWeatherCanvas(
    condition: WeatherCondition,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val colors = WeatherTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val frameTimeNanos = remember { mutableLongStateOf(0L) }

    val rainParticles = remember(condition) {
        if (condition == WeatherCondition.RAIN || condition == WeatherCondition.THUNDERSTORM || condition == WeatherCondition.SLEET) {
            List(110) {
                RainParticle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    length = Random.nextFloat() * 18f + 16f,
                    speed = Random.nextFloat() * 500f + 650f,
                    alpha = Random.nextFloat() * 0.35f + 0.15f
                )
            }
        } else emptyList()
    }

    val snowParticles = remember(condition) {
        if (condition == WeatherCondition.SNOW || condition == WeatherCondition.HAIL) {
            List(80) {
                SnowParticle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    radius = Random.nextFloat() * 2.8f + 1.8f,
                    speed = Random.nextFloat() * 55f + 40f,
                    sway = Random.nextFloat() * 16f + 10f,
                    phase = Random.nextFloat() * (2f * PI.toFloat())
                )
            }
        } else emptyList()
    }

    LaunchedEffect(condition) {
        var previousNanos = 0L
        while (true) {
            withFrameNanos { currentNanos ->
                if (previousNanos != 0L) {
                    val dt = ((currentNanos - previousNanos) / 1_000_000_000f).coerceAtMost(0.05f)

                    rainParticles.forEach { p ->
                        p.y += p.speed * dt
                        p.x -= p.speed * 0.12f * dt
                        if (p.y > 1.1f || p.x < -0.1f) {
                            p.y = -0.1f
                            p.x = Random.nextFloat() * 1.2f
                        }
                    }

                    snowParticles.forEach { p ->
                        p.y += p.speed * dt
                        p.phase += dt * 1.6f
                        if (p.y > 1.1f) {
                            p.y = -0.05f
                            p.x = Random.nextFloat()
                        }
                    }
                }
                previousNanos = currentNanos
                frameTimeNanos.longValue = currentNanos
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val timeSec = frameTimeNanos.longValue / 1_000_000_000f

        if (condition == WeatherCondition.CLEAR_DAY || condition == WeatherCondition.CLEAR_NIGHT) {
            val glowColor = if (condition == WeatherCondition.CLEAR_DAY) primaryColor else secondaryColor
            val breathe = (sin(timeSec * 0.8f) * 0.5f + 0.5f) * 0.08f + 0.04f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = breathe), Color.Transparent),
                    center = Offset(w * 0.82f, h * 0.12f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.82f, h * 0.12f)
            )
        }

        if (condition == WeatherCondition.FOG || condition == WeatherCondition.CLOUDY ||
            condition == WeatherCondition.PARTLY_CLOUDY_DAY || condition == WeatherCondition.PARTLY_CLOUDY_NIGHT
        ) {
            val fogAlpha = (sin(timeSec * 0.5f) * 0.5f + 0.5f) * 0.07f + 0.06f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = fogAlpha),
                        Color.Transparent,
                        primaryColor.copy(alpha = fogAlpha * 0.5f)
                    ),
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )
        }

        if (rainParticles.isNotEmpty()) {
            val rainColor = if (colors.isDark) Color(0xFFC4D5FF) else primaryColor
            val tiltX = -10f
            rainParticles.forEach { p ->
                val startX = p.x * w
                val startY = p.y * h
                drawLine(
                    color = rainColor.copy(alpha = p.alpha),
                    start = Offset(startX, startY),
                    end = Offset(startX + tiltX, startY + p.length),
                    strokeWidth = 2.0f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (snowParticles.isNotEmpty()) {
            val snowColor = if (colors.isDark) Color.White else Color(0xFF7A84B0)
            snowParticles.forEach { p ->
                val px = (p.x * w) + sin(p.phase) * p.sway
                val py = p.y * h
                drawCircle(
                    color = snowColor.copy(alpha = 0.78f),
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }
        }
    }
}