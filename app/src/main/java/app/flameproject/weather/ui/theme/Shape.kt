package app.flameproject.weather.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Consistent corner radiuses across application surfaces.
 */
object AppShapes {
    val Pill = RoundedCornerShape(percent = 50)
    val Card = RoundedCornerShape(28.dp)
    val Input = RoundedCornerShape(20.dp)
    val Dialog = RoundedCornerShape(24.dp)
}