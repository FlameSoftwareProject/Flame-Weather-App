
package app.flameproject.weather.ui.screens.settings
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flameproject.weather.R
import app.flameproject.weather.BuildConfig
import app.flameproject.weather.data.update.UpdateChecker
import app.flameproject.weather.data.local.AppLanguage
import app.flameproject.weather.data.local.DarkModePreference
import app.flameproject.weather.data.local.PaletteSeed
import app.flameproject.weather.data.local.PaletteStyle
import app.flameproject.weather.data.local.UserPreferences
import app.flameproject.weather.ui.motion.AppMotion
import app.flameproject.weather.ui.locale.localized
import app.flameproject.weather.ui.motion.bounceClickable
import app.flameproject.weather.ui.theme.AppShapes
import app.flameproject.weather.ui.theme.generateColorScheme
import app.flameproject.weather.ui.theme.resolveColorSeed
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onDarkModeChanged: (DarkModePreference) -> Unit,
    onPureBlackChanged: (Boolean) -> Unit,
    onPaletteSeedChanged: (PaletteSeed) -> Unit,
    onPaletteStyleChanged: (PaletteStyle) -> Unit,
    onCustomColorHexChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onWeatherApiUrlChanged: (String) -> Unit,
    onWeatherUserAgentChanged: (String) -> Unit,
    onWeatherApiKeyChanged: (String) -> Unit,
    onGeocodingUrlChanged: (String) -> Unit,
    onGeocodingUserAgentChanged: (String) -> Unit,
    onGeocodingApiKeyChanged: (String) -> Unit,
    onMapTileUrlChanged: (String) -> Unit,
    onMapTileUserAgentChanged: (String) -> Unit,
    onMapTileApiKeyChanged: (String) -> Unit,
    onResetEndpoints: () -> Unit,
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val localizedContext = remember(preferences.language) { context.localized(preferences.language) }
    var showEndpointInputs by remember { mutableStateOf(false) }
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isColorPickerDialogVisible by remember { mutableStateOf(false) }
    var isAboutDialogOpen by remember { mutableStateOf(false) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    val scope = rememberCoroutineScope()
    val githubIconId = remember {
        context.resources.getIdentifier("github", "drawable", context.packageName)
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(text = stringResource(R.string.settings_dark_mode), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModeSegment(
                            icon = Icons.Default.BrightnessAuto,
                            label = stringResource(R.string.mode_system),
                            selected = preferences.darkMode == DarkModePreference.SYSTEM,
                            onClick = { onDarkModeChanged(DarkModePreference.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeSegment(
                            icon = Icons.Default.LightMode,
                            label = stringResource(R.string.mode_light),
                            selected = preferences.darkMode == DarkModePreference.LIGHT,
                            onClick = { onDarkModeChanged(DarkModePreference.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeSegment(
                            icon = Icons.Default.DarkMode,
                            label = stringResource(R.string.mode_dark),
                            selected = preferences.darkMode == DarkModePreference.DARK,
                            onClick = { onDarkModeChanged(DarkModePreference.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_palette),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (preferences.paletteSeed != PaletteSeed.DYNAMIC) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ColorStyleOption(
                                label = stringResource(R.string.color_style_soft),
                                selected = preferences.paletteStyle == PaletteStyle.TONAL_SPOT,
                                onClick = { onPaletteStyleChanged(PaletteStyle.TONAL_SPOT) },
                                modifier = Modifier.weight(1f)
                            )
                            ColorStyleOption(
                                label = stringResource(R.string.color_style_vivid),
                                selected = preferences.paletteStyle == PaletteStyle.FIDELITY,
                                onClick = { onPaletteStyleChanged(PaletteStyle.FIDELITY) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    val isDarkMode = when (preferences.darkMode) {
                        DarkModePreference.LIGHT -> false
                        DarkModePreference.DARK -> true
                        DarkModePreference.SYSTEM -> isSystemInDarkTheme()
                    }
                    val dynamicScheme = remember(context, isDarkMode) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                        } else null
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.DYNAMIC, preferences.customColorHex),
                                label = stringResource(R.string.mode_system),
                                overrideScheme = dynamicScheme,
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.DYNAMIC,
                                onClick = { onPaletteSeedChanged(PaletteSeed.DYNAMIC) }
                            )
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.LAVENDER, preferences.customColorHex),
                                label = stringResource(R.string.palette_lavender),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.LAVENDER,
                                onClick = { onPaletteSeedChanged(PaletteSeed.LAVENDER) }
                            )
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.MINT, preferences.customColorHex),
                                label = stringResource(R.string.palette_mint),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.MINT,
                                onClick = { onPaletteSeedChanged(PaletteSeed.MINT) }
                            )
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.MATCHA, preferences.customColorHex),
                                label = stringResource(R.string.palette_matcha),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.MATCHA,
                                onClick = { onPaletteSeedChanged(PaletteSeed.MATCHA) }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.MARIGOLD, preferences.customColorHex),
                                label = stringResource(R.string.palette_marigold),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.MARIGOLD,
                                onClick = { onPaletteSeedChanged(PaletteSeed.MARIGOLD) }
                            )
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.ROSE, preferences.customColorHex),
                                label = stringResource(R.string.palette_rose),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.ROSE,
                                onClick = { onPaletteSeedChanged(PaletteSeed.ROSE) }
                            )
                            PaletteSwatch(
                                seed = resolveColorSeed(PaletteSeed.CUSTOM, preferences.customColorHex),
                                label = stringResource(R.string.palette_custom),
                                isDark = isDarkMode,
                                style = preferences.paletteStyle,
                                selected = preferences.paletteSeed == PaletteSeed.CUSTOM,
                                onClick = { onPaletteSeedChanged(PaletteSeed.CUSTOM) },
                                badge = {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                shape = CircleShape
                                            )
                                            .clickable { isColorPickerDialogVisible = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.settings_pure_black), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = preferences.pureBlack, onCheckedChange = onPureBlackChanged)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLanguageDialogOpen = true }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(text = stringResource(R.string.settings_language), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        text = when (preferences.language) {
                            AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
                            AppLanguage.EN -> stringResource(R.string.lang_en)
                            AppLanguage.ZH_HANS -> stringResource(R.string.lang_zh_hans)
                            AppLanguage.ZH_HANT -> stringResource(R.string.lang_zh_hant)
                            AppLanguage.JA -> stringResource(R.string.lang_ja)
                            AppLanguage.KO -> stringResource(R.string.lang_ko)
                            AppLanguage.RU -> stringResource(R.string.lang_ru)
                            AppLanguage.ES -> stringResource(R.string.lang_es)
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        item {
            var draftWeatherUrl by remember { mutableStateOf(preferences.weatherApiUrl) }
            var draftWeatherUa by remember { mutableStateOf(preferences.weatherUserAgent) }
            var draftWeatherKey by remember { mutableStateOf(preferences.weatherApiKey) }
            var draftGeocodingUrl by remember { mutableStateOf(preferences.customGeocodingUrl) }
            var draftGeocodingUa by remember { mutableStateOf(preferences.geocodingUserAgent) }
            var draftGeocodingKey by remember { mutableStateOf(preferences.geocodingApiKey) }
            var draftMapUrl by remember { mutableStateOf(preferences.customMapTileUrl) }
            var draftMapUa by remember { mutableStateOf(preferences.mapTileUserAgent) }
            var draftMapKey by remember { mutableStateOf(preferences.mapTileApiKey) }
            LaunchedEffect(preferences) {
                if (!showEndpointInputs) {
                    draftWeatherUrl = preferences.weatherApiUrl
                    draftWeatherUa = preferences.weatherUserAgent
                    draftWeatherKey = preferences.weatherApiKey
                    draftGeocodingUrl = preferences.customGeocodingUrl
                    draftGeocodingUa = preferences.geocodingUserAgent
                    draftGeocodingKey = preferences.geocodingApiKey
                    draftMapUrl = preferences.customMapTileUrl
                    draftMapUa = preferences.mapTileUserAgent
                    draftMapKey = preferences.mapTileApiKey
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.settings_endpoints), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = if (showEndpointInputs) stringResource(R.string.settings_endpoints_collapse) else stringResource(R.string.settings_endpoints_expand),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showEndpointInputs = !showEndpointInputs }
                        )
                    }
                    Text(
                        text = stringResource(R.string.endpoint_fallback_notice),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                    AnimatedVisibility(
                        visible = showEndpointInputs,
                        enter = expandVertically(AppMotion.cubicSpec()) + fadeIn(),
                        exit = shrinkVertically(AppMotion.cubicSpec()) + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = stringResource(R.string.endpoint_weather_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                                OutlinedTextField(
                                    value = draftWeatherUrl,
                                    onValueChange = { draftWeatherUrl = it },
                                    label = { Text(stringResource(R.string.url_template_hint)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                PresetRow(
                                    listOf(
                                        stringResource(R.string.preset_flame_default) to "https://weather.flameapis.com/",
                                        stringResource(R.string.preset_met_norway) to "https://api.met.no/weatherapi/locationforecast/2.0/compact"
                                    )
                                ) { draftWeatherUrl = it }
                                                                OutlinedTextField(
                                    value = draftWeatherUa,
                                    onValueChange = { draftWeatherUa = it },
                                    label = { Text(stringResource(R.string.label_user_agent)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                OutlinedTextField(
                                    value = draftWeatherKey,
                                    onValueChange = { draftWeatherKey = it },
                                    label = { Text(stringResource(R.string.label_api_key)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = stringResource(R.string.endpoint_geocoding_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                                OutlinedTextField(
                                    value = draftGeocodingUrl,
                                    onValueChange = { draftGeocodingUrl = it },
                                    label = { Text(stringResource(R.string.url_template_hint)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                PresetRow(
                                    listOf(
                                        stringResource(R.string.preset_open_meteo) to "https://geocoding-api.open-meteo.com/v1/search",
                                        stringResource(R.string.preset_nominatim) to "https://nominatim.openstreetmap.org/search",
                                        stringResource(R.string.preset_photon) to "https://photon.komoot.io/api/"
                                    )
                                ) { draftGeocodingUrl = it }
                                                                OutlinedTextField(
                                    value = draftGeocodingUa,
                                    onValueChange = { draftGeocodingUa = it },
                                    label = { Text(stringResource(R.string.label_user_agent)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                OutlinedTextField(
                                    value = draftGeocodingKey,
                                    onValueChange = { draftGeocodingKey = it },
                                    label = { Text(stringResource(R.string.label_api_key)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = stringResource(R.string.endpoint_map_tile_title), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                                OutlinedTextField(
                                    value = draftMapUrl,
                                    onValueChange = { draftMapUrl = it },
                                    label = { Text(stringResource(R.string.url_zxy_hint)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                PresetRow(
                                    listOf(
                                        stringResource(R.string.preset_google_sat) to "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}",
                                        stringResource(R.string.preset_osm) to "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                                        stringResource(R.string.preset_gaode_sat) to "https://webst01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=6&x={x}&y={y}&z={z}",
                                        stringResource(R.string.preset_cartodb) to "https://basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                    )
                                ) { draftMapUrl = it }
                                                                OutlinedTextField(
                                    value = draftMapUa,
                                    onValueChange = { draftMapUa = it },
                                    label = { Text(stringResource(R.string.label_user_agent)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                                                OutlinedTextField(
                                    value = draftMapKey,
                                    onValueChange = { draftMapKey = it },
                                    label = { Text(stringResource(R.string.label_api_key_replace)) },
                                    shape = AppShapes.Pill,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.endpoint_reset),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable {
                                        onResetEndpoints()
                                        showEndpointInputs = false
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(AppShapes.Pill)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .bounceClickable(onClick = {
                                            onWeatherApiUrlChanged(draftWeatherUrl)
                                            onWeatherUserAgentChanged(draftWeatherUa)
                                            onWeatherApiKeyChanged(draftWeatherKey)
                                            onGeocodingUrlChanged(draftGeocodingUrl)
                                            onGeocodingUserAgentChanged(draftGeocodingUa)
                                            onGeocodingApiKeyChanged(draftGeocodingKey)
                                            onMapTileUrlChanged(draftMapUrl)
                                            onMapTileUserAgentChanged(draftMapUa)
                                            onMapTileApiKeyChanged(draftMapKey)
                                            showEndpointInputs = false
                                        })
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(stringResource(R.string.btn_save_apply), fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAboutDialogOpen = true }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(text = stringResource(R.string.settings_about), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    if (isAboutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAboutDialogOpen = false },
            title = {
                Text(
                    text = localizedContext.getString(R.string.settings_about),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = localizedContext.getString(R.string.about_privacy_statement),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = localizedContext.getString(R.string.about_datasource_statement),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = localizedContext.getString(R.string.credits_ai_support),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { safeOpenUrl(context, "https://flameproject.net") }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = localizedContext.getString(R.string.link_main_site), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { safeOpenUrl(context, "https://flameproject.app/weather") }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = localizedContext.getString(R.string.link_web_app), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = localizedContext.getString(R.string.link_short_url),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (val state = updateState) {
                                    is UpdateState.Available -> safeOpenUrl(context, state.htmlUrl)
                                    UpdateState.Checking -> Unit
                                    else -> scope.launch {
                                        updateState = UpdateState.Checking
                                        updateState = runCatching { UpdateChecker.fetchLatestRelease() }.fold(
                                            onSuccess = { release ->
                                                when {
                                                    release == null -> UpdateState.NoRelease
                                                    UpdateChecker.isNewer(release.tagName, BuildConfig.VERSION_NAME) ->
                                                        UpdateState.Available(release.tagName, release.htmlUrl)
                                                    else -> UpdateState.UpToDate
                                                }
                                            },
                                            onFailure = { UpdateState.Failed }
                                        )
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SystemUpdate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(text = localizedContext.getString(R.string.check_updates), fontSize = 13.sp)
                        }
                        Text(
                            text = "v" + BuildConfig.VERSION_NAME,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    when (val state = updateState) {
                        UpdateState.Checking -> Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        }
                        is UpdateState.Available -> Text(
                            text = localizedContext.getString(R.string.update_available, state.tagName),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        UpdateState.UpToDate -> Text(
                            text = localizedContext.getString(R.string.update_latest),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        UpdateState.NoRelease -> Text(
                            text = localizedContext.getString(R.string.update_none),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        UpdateState.Failed -> Text(
                            text = localizedContext.getString(R.string.update_failed),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        UpdateState.Idle -> Unit
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { safeOpenUrl(context, "https://github.com/FlameSoftwareProject/Flame-Weather-App") }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (githubIconId != 0) {
                                Icon(painter = painterResource(id = githubIconId), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }
                            Text(text = localizedContext.getString(R.string.credits_github), fontSize = 13.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isAboutDialogOpen = false }) {
                    Text(localizedContext.getString(R.string.btn_close))
                }
            }
        )
    }
    if (isColorPickerDialogVisible) {
        var hue by remember { mutableFloatStateOf(174f) }
        var saturation by remember { mutableFloatStateOf(1f) }
        var value by remember { mutableFloatStateOf(0.6f) }
        val currentColor = Color.hsv(hue, saturation, value)
        val hexString = String.format("#%06X", 0xFFFFFF and currentColor.toArgb())
        val isColorDark = (0.299f * currentColor.red + 0.587f * currentColor.green + 0.114f * currentColor.blue) < 0.5f
        val hexTextColor = if (isColorDark) Color.White else Color.Black
        AlertDialog(
            onDismissRequest = { isColorPickerDialogVisible = false },
            title = { Text(localizedContext.getString(R.string.dialog_custom_color), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val pos = change.position
                                        val dx = pos.x - centerX
                                        val dy = pos.y - centerY
                                        val radius = sqrt(dx * dx + dy * dy)
                                        val outerRadius = (minOf(size.width, size.height)) / 2f
                                        val innerRadius = outerRadius - 32.dp.toPx()
                                        if (radius in innerRadius..outerRadius) {
                                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                            if (angle < 0) angle += 360f
                                            hue = angle
                                        }
                                    }
                                }
                        ) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val center = Offset(centerX, centerY)
                            val outerRadius = (minOf(size.width, size.height)) / 2f
                            val strokeWidth = 28.dp.toPx()
                            val innerRadius = outerRadius - strokeWidth / 2f
                            val sweepGradient = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                ),
                                center = center
                            )
                            drawCircle(
                                brush = sweepGradient,
                                radius = innerRadius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )
                            val rad = Math.toRadians(hue.toDouble())
                            val handleX = centerX + innerRadius * cos(rad).toFloat()
                            val handleY = centerY + innerRadius * sin(rad).toFloat()
                            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(handleX, handleY))
                            drawCircle(color = Color.hsv(hue, 1f, 1f), radius = 7.dp.toPx(), center = Offset(handleX, handleY))
                        }
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        val w = size.width.toFloat()
                                        val h = size.height.toFloat()
                                        if (w > 0 && h > 0) {
                                            saturation = (change.position.x / w).coerceIn(0f, 1f)
                                            value = (1f - change.position.y / h).coerceIn(0f, 1f)
                                        }
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.White, Color.hsv(hue, 1f, 1f))
                                    )
                                )
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black)
                                    )
                                )
                                val handlePos = Offset(saturation * size.width, (1f - value) * size.height)
                                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = handlePos, style = Stroke(width = 2.dp.toPx()))
                                drawCircle(color = currentColor, radius = 6.dp.toPx(), center = handlePos)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(AppShapes.Pill)
                            .background(currentColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, AppShapes.Pill)
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = hexString,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = hexTextColor,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onCustomColorHexChanged(hexString)
                    onPaletteSeedChanged(PaletteSeed.CUSTOM)
                    isColorPickerDialogVisible = false
                }) {
                    Text(localizedContext.getString(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isColorPickerDialogVisible = false }) {
                    Text(localizedContext.getString(R.string.btn_cancel))
                }
            }
        )
    }
    if (isLanguageDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLanguageDialogOpen = false },
            title = { Text(text = localizedContext.getString(R.string.settings_language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = preferences.language == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    onLanguageChanged(lang)
                                    isLanguageDialogOpen = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SYSTEM -> localizedContext.getString(R.string.lang_system)
                                    AppLanguage.EN -> localizedContext.getString(R.string.lang_en)
                                    AppLanguage.ZH_HANS -> localizedContext.getString(R.string.lang_zh_hans)
                                    AppLanguage.ZH_HANT -> localizedContext.getString(R.string.lang_zh_hant)
                                    AppLanguage.JA -> localizedContext.getString(R.string.lang_ja)
                                    AppLanguage.KO -> localizedContext.getString(R.string.lang_ko)
                                    AppLanguage.RU -> localizedContext.getString(R.string.lang_ru)
                                    AppLanguage.ES -> localizedContext.getString(R.string.lang_es)
                                },
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (selected) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isLanguageDialogOpen = false }) {
                    Text(localizedContext.getString(R.string.btn_close))
                }
            }
        )
    }}
private fun safeOpenUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }}

private sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data object NoRelease : UpdateState
    data object Failed : UpdateState
    data class Available(val tagName: String, val htmlUrl: String) : UpdateState
}

@Composable
private fun PresetRow(presets: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(presets) { preset ->
            Box(
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), AppShapes.Pill)
                    .clickable { onSelect(preset.second) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = preset.first, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }}

@Composable
private fun ModeSegment(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }}

@Composable
private fun ColorStyleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }}

@Composable
private fun PaletteSwatch(
    seed: Color,
    label: String,
    overrideScheme: ColorScheme? = null,
    isDark: Boolean = false,
    style: PaletteStyle = PaletteStyle.TONAL_SPOT,
    selected: Boolean,
    onClick: () -> Unit,
    badge: (@Composable () -> Unit)? = null) {
    val scheme: ColorScheme = overrideScheme ?: remember(seed, isDark, style) { generateColorScheme(seed, isDark, style) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
    Box {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    width = 2.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(38.dp)) {
                drawArc(
                    color = scheme.primary,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    style = Fill
                )
                drawArc(
                    color = scheme.secondary,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = true,
                    style = Fill
                )
                drawArc(
                    color = scheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = true,
                    style = Fill
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        badge?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp),
                contentAlignment = Alignment.Center
            ) { it() }
        }
    }
    Text(
        text = label,
        fontSize = 9.sp,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    }}
