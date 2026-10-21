package app.flameproject.weather.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import app.flameproject.weather.data.local.PaletteSeed
import app.flameproject.weather.data.local.PaletteStyle
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeFidelity
import com.google.android.material.color.utilities.SchemeTonalSpot

@Immutable
data class AppColors(
    val bgBase: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderBase: Color,
    val pillBg: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentSoft: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        bgBase = Color(0xFFFCFCFC),
        cardBg = Color(0xFFF3F3F6),
        textPrimary = Color(0xFF1C1B1F),
        textSecondary = Color(0xFF49454F),
        borderBase = Color(0x33CAC4D0),
        pillBg = Color(0xFFEBEBF0),
        accentPrimary = Color(0xFF2196F3),
        accentSecondary = Color(0xFF03A9F4),
        accentSoft = Color(0x262196F3),
        isDark = false
    )
}

/**
 * Theme scoped accessors for the application palette and shape system.
 */
object WeatherTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = AppShapes

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

/**
 * Generates a Material You color scheme with the official HCT based algorithm
 * shipped in the Material Components library.
 */
fun generateColorScheme(seedColor: Color, isDark: Boolean, style: PaletteStyle = PaletteStyle.TONAL_SPOT): ColorScheme {
    val hct = Hct.fromInt(seedColor.toArgb())
    val scheme = when (style) {
        PaletteStyle.TONAL_SPOT -> SchemeTonalSpot(hct, isDark, 0.0)
        PaletteStyle.FIDELITY -> SchemeFidelity(hct, isDark, 0.0)
    }
    val roles = MaterialDynamicColors()
    fun role(color: DynamicColor): Color = Color(color.getArgb(scheme))

    return if (isDark) {
        darkColorScheme(
            primary = role(roles.primary()),
            onPrimary = role(roles.onPrimary()),
            primaryContainer = role(roles.primaryContainer()),
            onPrimaryContainer = role(roles.onPrimaryContainer()),
            inversePrimary = role(roles.inversePrimary()),
            secondary = role(roles.secondary()),
            onSecondary = role(roles.onSecondary()),
            secondaryContainer = role(roles.secondaryContainer()),
            onSecondaryContainer = role(roles.onSecondaryContainer()),
            tertiary = role(roles.tertiary()),
            onTertiary = role(roles.onTertiary()),
            tertiaryContainer = role(roles.tertiaryContainer()),
            onTertiaryContainer = role(roles.onTertiaryContainer()),
            background = role(roles.background()),
            onBackground = role(roles.onBackground()),
            surface = role(roles.surface()),
            onSurface = role(roles.onSurface()),
            surfaceVariant = role(roles.surfaceVariant()),
            onSurfaceVariant = role(roles.onSurfaceVariant()),
            surfaceTint = role(roles.surfaceTint()),
            inverseSurface = role(roles.inverseSurface()),
            inverseOnSurface = role(roles.inverseOnSurface()),
            error = role(roles.error()),
            onError = role(roles.onError()),
            errorContainer = role(roles.errorContainer()),
            onErrorContainer = role(roles.onErrorContainer()),
            outline = role(roles.outline()),
            outlineVariant = role(roles.outlineVariant()),
            scrim = role(roles.scrim()),
            surfaceBright = role(roles.surfaceBright()),
            surfaceDim = role(roles.surfaceDim()),
            surfaceContainer = role(roles.surfaceContainer()),
            surfaceContainerHigh = role(roles.surfaceContainerHigh()),
            surfaceContainerHighest = role(roles.surfaceContainerHighest()),
            surfaceContainerLow = role(roles.surfaceContainerLow()),
            surfaceContainerLowest = role(roles.surfaceContainerLowest())
        )
    } else {
        lightColorScheme(
            primary = role(roles.primary()),
            onPrimary = role(roles.onPrimary()),
            primaryContainer = role(roles.primaryContainer()),
            onPrimaryContainer = role(roles.onPrimaryContainer()),
            inversePrimary = role(roles.inversePrimary()),
            secondary = role(roles.secondary()),
            onSecondary = role(roles.onSecondary()),
            secondaryContainer = role(roles.secondaryContainer()),
            onSecondaryContainer = role(roles.onSecondaryContainer()),
            tertiary = role(roles.tertiary()),
            onTertiary = role(roles.onTertiary()),
            tertiaryContainer = role(roles.tertiaryContainer()),
            onTertiaryContainer = role(roles.onTertiaryContainer()),
            background = role(roles.background()),
            onBackground = role(roles.onBackground()),
            surface = role(roles.surface()),
            onSurface = role(roles.onSurface()),
            surfaceVariant = role(roles.surfaceVariant()),
            onSurfaceVariant = role(roles.onSurfaceVariant()),
            surfaceTint = role(roles.surfaceTint()),
            inverseSurface = role(roles.inverseSurface()),
            inverseOnSurface = role(roles.inverseOnSurface()),
            error = role(roles.error()),
            onError = role(roles.onError()),
            errorContainer = role(roles.errorContainer()),
            onErrorContainer = role(roles.onErrorContainer()),
            outline = role(roles.outline()),
            outlineVariant = role(roles.outlineVariant()),
            scrim = role(roles.scrim()),
            surfaceBright = role(roles.surfaceBright()),
            surfaceDim = role(roles.surfaceDim()),
            surfaceContainer = role(roles.surfaceContainer()),
            surfaceContainerHigh = role(roles.surfaceContainerHigh()),
            surfaceContainerHighest = role(roles.surfaceContainerHighest()),
            surfaceContainerLow = role(roles.surfaceContainerLow()),
            surfaceContainerLowest = role(roles.surfaceContainerLowest())
        )
    }
}

/**
 * Baseline key colors from the official Material palette (material.io).
 */
fun resolveColorSeed(seed: PaletteSeed, customHex: String): Color = when (seed) {
    PaletteSeed.DYNAMIC -> Color(0xFF6750A4)
    PaletteSeed.LAVENDER -> Color(0xFF6750A4)
    PaletteSeed.MINT -> Color(0xFF00696E)
    PaletteSeed.MATCHA -> Color(0xFF386A20)
    PaletteSeed.MARIGOLD -> Color(0xFF7D5700)
    PaletteSeed.ROSE -> Color(0xFF984061)
    PaletteSeed.CUSTOM -> runCatching { Color(android.graphics.Color.parseColor(customHex)) }.getOrDefault(Color(0xFF6750A4))
}

@Composable
fun WeatherTheme(
    seed: PaletteSeed = PaletteSeed.DYNAMIC,
    customHex: String = "#009688",
    colorStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
    pureBlack: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val rawColorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && seed == PaletteSeed.DYNAMIC) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        generateColorScheme(resolveColorSeed(seed, customHex), darkTheme, colorStyle)
    }

    val finalColorScheme = if (darkTheme && pureBlack) {
        rawColorScheme.copy(
            background = Color.Black,
            surface = Color(0xFF0D0D0D),
            surfaceContainer = Color(0xFF161616),
            surfaceContainerHigh = Color(0xFF222222)
        )
    } else rawColorScheme

    val isLight = !darkTheme

    val appColors = AppColors(
        bgBase = if (isLight) finalColorScheme.surfaceContainerLow else finalColorScheme.background,
        cardBg = if (isLight) finalColorScheme.surface else finalColorScheme.surfaceContainer,
        textPrimary = finalColorScheme.onSurface,
        textSecondary = finalColorScheme.onSurfaceVariant,
        borderBase = finalColorScheme.outlineVariant.copy(alpha = if (isLight) 0.75f else 0.35f),
        pillBg = finalColorScheme.surfaceContainerHigh,
        accentPrimary = finalColorScheme.primary,
        accentSecondary = finalColorScheme.tertiary,
        accentSoft = finalColorScheme.primaryContainer.copy(alpha = 0.5f),
        isDark = darkTheme
    )

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
