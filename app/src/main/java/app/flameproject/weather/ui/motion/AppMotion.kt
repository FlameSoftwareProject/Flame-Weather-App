package app.flameproject.weather.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

/**
 * Editorial motion system using pure cubic-bezier easing curves without bouncy springs.
 */
object AppMotion {

    val SmoothCubicEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardCubicEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    fun <T> cubicSpec(durationMillis: Int = 260) = tween<T>(
        durationMillis = durationMillis,
        easing = SmoothCubicEasing
    )

    fun <T> fastSpec(durationMillis: Int = 180) = tween<T>(
        durationMillis = durationMillis,
        easing = SmoothCubicEasing
    )
}

/**
 * Adds smooth cubic deceleration micro-scaling on touch without oscillatory bounce.
 */
fun Modifier.bounceClickable(
    minScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) minScale else 1f,
        animationSpec = AppMotion.fastSpec(),
        label = "CubicScale"
    )

    this
        .scale(animatedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}