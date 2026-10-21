package app.flameproject.weather.ui.screens.home

import android.widget.Toast

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flameproject.weather.R
import app.flameproject.weather.data.local.TemperatureUnit
import app.flameproject.weather.data.remote.MapTileClient
import app.flameproject.weather.domain.model.DailyForecast
import app.flameproject.weather.domain.model.HourlyForecast
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherCondition
import app.flameproject.weather.domain.model.WeatherData
import app.flameproject.weather.ui.export.CardExporter
import app.flameproject.weather.ui.export.CardSnapshot
import app.flameproject.weather.ui.export.ShareCard
import app.flameproject.weather.ui.motion.AppMotion
import app.flameproject.weather.ui.motion.bounceClickable
import app.flameproject.weather.ui.theme.WeatherTheme
import app.flameproject.weather.ui.viewmodel.WeatherErrorType
import app.flameproject.weather.ui.viewmodel.WeatherUiState
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Locale
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.tan

@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    customMapTileUrl: String = "",
    onSearchQueryChanged: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onLocateClicked: () -> Unit,
    onUnitToggleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isMapExpanded by remember { mutableStateOf(false) }
    val colors = WeatherTheme.colors

    BackHandler(enabled = isMapExpanded) {
        isMapExpanded = false
    }

    val context = LocalContext.current
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= 600
    val coroutineScope = rememberCoroutineScope()
    val captureLayer = rememberGraphicsLayer()
    var captureTick by remember { mutableIntStateOf(0) }
    var isExporting by remember { mutableStateOf(false) }
    var cardMosaic by remember { mutableStateOf<ImageBitmap?>(null) }
    val cardSnapshot = shareCardSnapshot(uiState = uiState)

    fun exportWeatherCard() {
        val selectedPlace = uiState.place
        if (isExporting || selectedPlace == null || uiState.weatherData == null) return
        coroutineScope.launch {
            isExporting = true
            val succeeded = runCatching {
                val mosaic = CardExporter.buildMosaic(
                    context = context,
                    latitude = selectedPlace.latitude,
                    longitude = selectedPlace.longitude,
                    zoom = 11,
                    width = 1080,
                    height = 1350,
                    tileSize = 256.0,
                    tileUrlTemplate = customMapTileUrl.ifBlank { uiState.activeTileUrl }
                )
                cardMosaic = mosaic
                captureTick++
                withFrameNanos { }
                withFrameNanos { }
                val bitmap = captureLayer.toImageBitmap()
                CardExporter.saveAndShare(context, bitmap, "flameweather_card_${System.currentTimeMillis()}.png")
            }.onFailure {
                android.util.Log.e("CardExport", "export failed", it)
            }.getOrDefault(false)
            if (!succeeded) {
                Toast.makeText(context, context.getString(R.string.export_toast_failed), Toast.LENGTH_SHORT).show()
            }
            isExporting = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ShareCard(
            snapshot = cardSnapshot,
            mosaic = cardMosaic,
            modifier = Modifier
                .requiredSize(360.dp, 450.dp)
                .graphicsLayer { alpha = 0f }
                .drawWithContent {
                    captureTick
                    captureLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawContent()
                }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChanged(it)
                        },
                        placeholder = { Text(text = stringResource(R.string.search_hint), color = colors.textSecondary, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.pillBg,
                            unfocusedContainerColor = colors.pillBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).clip(WeatherTheme.shapes.Pill).border(1.dp, colors.borderBase, WeatherTheme.shapes.Pill)
                    )

                    Box(
                        modifier = if (isWideScreen) {
                            Modifier.clip(WeatherTheme.shapes.Pill).background(colors.pillBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Pill).bounceClickable(onClick = ::exportWeatherCard).padding(horizontal = 14.dp, vertical = 12.dp)
                        } else {
                            Modifier.size(46.dp).clip(CircleShape).background(colors.pillBg).border(1.dp, colors.borderBase, CircleShape).bounceClickable(onClick = ::exportWeatherCard)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isExporting) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isWideScreen) 18.dp else 20.dp)
                                )
                            }
                            if (isWideScreen) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.action_export_card),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(colors.pillBg).border(1.dp, colors.borderBase, CircleShape).bounceClickable(onClick = onLocateClicked),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }

                    Box(
                        modifier = Modifier.clip(WeatherTheme.shapes.Pill).background(colors.pillBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Pill).bounceClickable(onClick = onUnitToggleClicked).padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (uiState.temperatureUnit == TemperatureUnit.CELSIUS) "°C" else "°F", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 13.sp)
                    }
                }
            }

            if (uiState.searchResults.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Input).background(colors.cardBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Input).padding(8.dp)
                    ) {
                        uiState.searchResults.forEach { place ->
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Input).clickable { searchQuery = ""; onPlaceSelected(place) }.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text(text = place.displayName, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (uiState.weatherData != null) {
                val weather = uiState.weatherData
                item { CompactClockStrip(deviceTimeText = uiState.deviceTimeText, localTimeText = uiState.localTimeText) }
                item { HeroWeatherCard(place = uiState.place, weather = weather, unit = uiState.temperatureUnit) }
                item { HourlyForecastSection(hourly = weather.hourly, unit = uiState.temperatureUnit) }
                item { DailyForecastSection(daily = weather.daily, unit = uiState.temperatureUnit) }
                item { MapPreviewCard(place = uiState.place, activeTileUrl = uiState.activeTileUrl, customUrl = customMapTileUrl, onExpand = { isMapExpanded = true }) }
            } else {
                item { EmptyStateCard(errorType = uiState.errorType, onRetry = onLocateClicked) }
            }
        }

        AnimatedVisibility(
            visible = isMapExpanded,
            enter = fadeIn(AppMotion.fastSpec()) + slideInVertically(AppMotion.cubicSpec()) { it },
            exit = fadeOut(AppMotion.fastSpec()) + slideOutVertically(AppMotion.cubicSpec()) { it }
        ) {
            ExpandedMapOverlay(place = uiState.place, activeTileUrl = uiState.activeTileUrl, customUrl = customMapTileUrl, onClose = { isMapExpanded = false })
        }
    }
}

@Composable
private fun CompactClockStrip(deviceTimeText: String, localTimeText: String) {
    val colors = WeatherTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Input).background(colors.cardBg.copy(alpha = 0.85f)).border(1.dp, colors.borderBase, WeatherTheme.shapes.Input).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.device_time), fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = deviceTimeText, style = WeatherTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1)
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.local_time), fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = localTimeText, style = WeatherTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}

@Composable
private fun HeroWeatherCard(place: Place?, weather: WeatherData, unit: TemperatureUnit) {
    val colors = WeatherTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Card).background(colors.cardBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Card).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = place?.displayName ?: "", style = WeatherTheme.typography.headlineLarge, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (place?.isCurrentLocation == true) {
                        Box(modifier = Modifier.clip(WeatherTheme.shapes.Pill).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(text = stringResource(R.string.my_location), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    place?.country?.let {
                        Text(text = it, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Icon(imageVector = resolveWeatherIcon(weather.current.condition), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = formatTemperature(weather.current.temperature, unit), style = WeatherTheme.typography.displayLarge, color = colors.textPrimary)
                Text(text = if (unit == TemperatureUnit.CELSIUS) "°C" else "°F", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, modifier = Modifier.padding(top = 10.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(icon = Icons.Default.WaterDrop, label = stringResource(R.string.humidity), value = "${weather.current.humidity?.toInt() ?: "--"}%")
                MetricChip(icon = Icons.Default.Air, label = stringResource(R.string.wind), value = "${weather.current.windSpeed ?: "--"} m/s")
                MetricChip(icon = Icons.Default.Compress, label = stringResource(R.string.pressure), value = "${weather.current.pressure?.toInt() ?: "--"} hPa")
            }
        }
    }
}

@Composable
private fun MetricChip(icon: ImageVector, label: String, value: String) {
    val colors = WeatherTheme.colors
    Row(
        modifier = Modifier.clip(WeatherTheme.shapes.Pill).background(colors.pillBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Pill).padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, maxLines = 1)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1)
    }
}

@Composable
private fun HourlyForecastSection(hourly: List<HourlyForecast>, unit: TemperatureUnit) {
    val colors = WeatherTheme.colors
    val appLocale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.forecast_hourly), style = WeatherTheme.typography.headlineMedium, color = colors.textPrimary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(hourly) { item ->
                Column(
                    modifier = Modifier.clip(WeatherTheme.shapes.Input).background(colors.cardBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Input).padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = item.time.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm", appLocale)), fontSize = 12.sp, color = colors.textSecondary)
                    Icon(imageVector = resolveWeatherIcon(item.condition), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(text = "${formatTemperature(item.temperature, unit)}°", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun DailyForecastSection(daily: List<DailyForecast>, unit: TemperatureUnit) {
    val colors = WeatherTheme.colors
    val appLocale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.forecast_daily), style = WeatherTheme.typography.headlineMedium, color = colors.textPrimary)
        Column(
            modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Card).background(colors.cardBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Card).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            daily.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = day.date.format(DateTimeFormatter.ofPattern("EEE", appLocale)), fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(imageVector = resolveWeatherIcon(day.condition), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    Row(modifier = Modifier.weight(1.4f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.North, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Text(text = "${formatTemperature(day.tempMax, unit)}°", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.South, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                            Text(text = "${formatTemperature(day.tempMin, unit)}°", color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeTileMap(
    lat: Double,
    lon: Double,
    placeName: String,
    initialZoom: Float,
    baseTileUrl: String,
    customUrl: String,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val tileSizePx = 256.0 * density
    val appContext = LocalContext.current.applicationContext

    var viewportPx by remember { mutableStateOf(IntSize.Zero) }
    var zoomFloat by remember(lat, lon) { mutableFloatStateOf(initialZoom) }
    var worldX by remember(lat, lon) { mutableDoubleStateOf(mercatorX(lon)) }
    var worldY by remember(lat, lon) { mutableDoubleStateOf(mercatorY(lat)) }

    val tileCache = remember { mutableStateMapOf<String, ImageBitmap>() }
    val inFlight = remember(customUrl, baseTileUrl) { mutableSetOf<String>() }

    val rawUrl = customUrl.ifBlank { baseTileUrl }
    val isCustom = customUrl.isNotBlank()
    val isGaode = !isCustom && rawUrl.contains("autonavi")
    val isOsm = !isCustom && rawUrl.contains("openstreetmap")
    val isGoogle = !isCustom && rawUrl.contains("google")

    val watermark = when {
        isCustom -> stringResource(R.string.custom_api)
        isGoogle -> stringResource(R.string.provider_google_maps)
        isOsm -> stringResource(R.string.provider_osm)
        isGaode -> stringResource(R.string.provider_gaode)
        else -> stringResource(R.string.map_data)
    }

    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(rawUrl, appContext) {
        snapshotFlow {
            visibleTileUrls(
                zoom = zoomFloat,
                worldX = worldX,
                worldY = worldY,
                viewport = viewportPx,
                tilePx = tileSizePx,
                template = rawUrl,
                gaodeRoad = isGaode
            )
        }.collect { urls ->
            urls.forEach { url ->
                if (url in tileCache || url in inFlight) return@forEach
                inFlight.add(url)
                launch {
                    val bitmap = MapTileClient.bitmap(appContext, url)
                    if (bitmap != null) tileCache[url] = bitmap
                    if (tileCache.size > 256) {
                        tileCache.keys.take(tileCache.size - 200).forEach { tileCache.remove(it) }
                    }
                    inFlight.remove(url)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color(0xFF161616))
            .let {
                if (isInteractive) {
                    it.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoomFactor, _ ->
                            val next = (zoomFloat + log2Of(zoomFactor)).coerceIn(3f, 19f)
                            zoomFloat = next
                            val worldSize = tileSizePx * 2.0.pow(next.toDouble())
                            worldX -= pan.x.toDouble() / worldSize
                            worldY -= pan.y.toDouble() / worldSize
                        }
                    }
                } else it
            }
            .onSizeChanged { viewportPx = it }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vp = viewportPx
            if (vp == IntSize.Zero) return@Canvas

            val zoomInt = floor(zoomFloat).toInt().coerceIn(0, 21)
            val tilesPerEdge = 1 shl zoomInt
            val worldPixel = tileSizePx * 2.0.pow(zoomFloat.toDouble())
            val centerX = vp.width / 2.0
            val centerY = vp.height / 2.0

            val minMercatorX = worldX - centerX / worldPixel
            val minMercatorY = worldY - centerY / worldPixel
            val maxMercatorX = worldX + (vp.width / 2.0) / worldPixel
            val maxMercatorY = worldY + (vp.height / 2.0) / worldPixel

            val startCol = floor(minMercatorX * tilesPerEdge).toInt() - 1
            val endCol = floor(maxMercatorX * tilesPerEdge).toInt() + 1
            val startRow = floor(minMercatorY * tilesPerEdge).toInt() - 1
            val endRow = floor(maxMercatorY * tilesPerEdge).toInt() + 1

            fun paintBitmap(
                bitmap: ImageBitmap,
                srcOffset: IntOffset,
                srcSize: IntSize,
                left: Double,
                top: Double,
                sizePx: Double
            ) {
                val scale = (sizePx / srcSize.width).toFloat().coerceAtLeast(0f)
                withTransform({
                    translate(left.toFloat(), top.toFloat())
                    scale(scale, scale, Offset.Zero)
                }) {
                    drawImage(bitmap, srcOffset = srcOffset, srcSize = srcSize)
                }
            }

            fun paintTile(
                template: String,
                z: Int,
                x: Int,
                y: Int,
                left: Double,
                top: Double,
                sizePx: Double,
                depth: Int
            ) {
                val perEdge = 1 shl z
                if (y < 0 || y >= perEdge) return
                val bitmap = tileCache[buildTileUrl(template, z, x.mod(perEdge), y, isGaode, isOsm)]
                if (bitmap != null) {
                    paintBitmap(bitmap, IntOffset.Zero, IntSize(bitmap.width, bitmap.height), left, top, sizePx)
                    return
                }
                if (depth >= 3) return

                if (z > 0) {
                    val parentX = x.floorDiv(2)
                    val parentY = y.floorDiv(2)
                    val parent = tileCache[buildTileUrl(template, z - 1, parentX, parentY, isGaode, isOsm)]
                    if (parent != null) {
                        val half = parent.width / 2
                        paintBitmap(
                            parent,
                            IntOffset((x % 2) * half, (y % 2) * half),
                            IntSize(half, half),
                            left,
                            top,
                            sizePx
                        )
                        return
                    }
                }

                for (qy in 0..1) {
                    for (qx in 0..1) {
                        paintTile(
                            template,
                            z + 1,
                            x * 2 + qx,
                            y * 2 + qy,
                            left + qx * sizePx / 2.0,
                            top + qy * sizePx / 2.0,
                            sizePx / 2.0,
                            depth + 1
                        )
                    }
                }
            }

            drawRect(Color(0xFF161616))

            for (r in startRow..endRow) {
                if (r < 0 || r >= tilesPerEdge) continue
                for (c in startCol..endCol) {
                    val wrappedC = c.mod(tilesPerEdge)
                    val left = centerX + (c.toDouble() / tilesPerEdge - worldX) * worldPixel
                    val top = centerY + (r.toDouble() / tilesPerEdge - worldY) * worldPixel
                    val sizePx = worldPixel / tilesPerEdge

                    paintTile(rawUrl, zoomInt, wrappedC, r, left, top, sizePx, 0)
                    if (isGaode) {
                        paintTile(buildRoadUrl(zoomInt, wrappedC, r), zoomInt, wrappedC, r, left, top, sizePx, 0)
                    }
                }
            }

            val pinX = centerX + (mercatorX(lon) - worldX) * worldPixel
            val pinY = centerY + (mercatorY(lat) - worldY) * worldPixel
            val pinRadius = 7.dp.toPx()
            drawCircle(Color(0xFFFF5722), radius = pinRadius, center = Offset(pinX.toFloat(), pinY.toFloat()))
            drawCircle(
                Color.White,
                radius = pinRadius,
                center = Offset(pinX.toFloat(), pinY.toFloat()),
                style = Stroke(width = 2.dp.toPx())
            )

            if (placeName.isNotBlank()) {
                val label = textMeasurer.measure(
                    placeName,
                    TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
                val boxLeft = pinX + 12.dp.toPx()
                val boxTop = pinY - 20.dp.toPx()
                val boxWidth = label.size.width + 20.dp.toPx()
                val boxHeight = label.size.height + 8.dp.toPx()
                drawRoundRect(
                    color = Color(0xDA16141A),
                    topLeft = Offset(boxLeft.toFloat(), boxTop.toFloat()),
                    size = Size(boxWidth.toFloat(), boxHeight.toFloat()),
                    cornerRadius = CornerRadius(boxHeight / 2f)
                )
                drawText(
                    label,
                    topLeft = Offset(
                        (boxLeft + 10.dp.toPx()).toFloat(),
                        (boxTop + 4.dp.toPx()).toFloat()
                    )
                )
            }

            val mark = textMeasurer.measure(
                watermark,
                TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
            )
            val markPadding = 6.dp.toPx()
            val markBoxWidth = mark.size.width + 12.dp.toPx()
            val markBoxHeight = mark.size.height + 4.dp.toPx()
            val markLeft = vp.width - markPadding - markBoxWidth
            val markTop = vp.height - markPadding - markBoxHeight
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(markLeft, markTop),
                size = Size(markBoxWidth, markBoxHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawText(
                mark,
                topLeft = Offset(markLeft + markPadding, markTop + 2.dp.toPx())
            )
        }
    }
}

private fun mercatorX(longitude: Double): Double = (longitude + 180.0) / 360.0

private fun mercatorY(latitude: Double): Double {
    val latRad = latitude * PI / 180.0
    return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0
}

private fun log2Of(zoomFactor: Float): Float =
    (ln(zoomFactor.toDouble()) / ln(2.0)).toFloat()

private fun buildTileUrl(
    template: String,
    zoom: Int,
    column: Int,
    row: Int,
    gaode: Boolean,
    osm: Boolean
): String {
    val subdomain = when {
        osm -> arrayOf("a", "b", "c")[abs(column + row) % 3]
        gaode -> ((abs(column + row) % 4) + 1).toString()
        else -> (abs(column + row) % 4).toString()
    }
    return template.replace("{x}", column.toString())
        .replace("{y}", row.toString())
        .replace("{z}", zoom.toString())
        .replace("{s}", subdomain)
}

private fun buildRoadUrl(zoom: Int, column: Int, row: Int): String =
    "https://webst0${((abs(column + row) % 4) + 1)}.is.autonavi.com/" +
        "appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=$column&y=$row&z=$zoom"

private fun visibleTileUrls(
    zoom: Float,
    worldX: Double,
    worldY: Double,
    viewport: IntSize,
    tilePx: Double,
    template: String,
    gaodeRoad: Boolean
): List<String> {
    if (viewport == IntSize.Zero) return emptyList()
    val zoomInt = floor(zoom).toInt().coerceIn(0, 21)
    val tilesPerEdge = 1 shl zoomInt
    val worldPixel = tilePx * 2.0.pow(zoom.toDouble())
    val minMercatorX = worldX - viewport.width / 2.0 / worldPixel
    val minMercatorY = worldY - viewport.height / 2.0 / worldPixel
    val maxMercatorX = worldX + viewport.width / 2.0 / worldPixel
    val maxMercatorY = worldY + viewport.height / 2.0 / worldPixel

    val startCol = floor(minMercatorX * tilesPerEdge).toInt() - 1
    val endCol = floor(maxMercatorX * tilesPerEdge).toInt() + 1
    val startRow = floor(minMercatorY * tilesPerEdge).toInt() - 1
    val endRow = floor(maxMercatorY * tilesPerEdge).toInt() + 1

    val gaode = template.contains("autonavi")
    val osm = template.contains("openstreetmap")

    return buildList {
        for (r in startRow..endRow) {
            if (r < 0 || r >= tilesPerEdge) continue
            for (c in startCol..endCol) {
                val wrappedC = c.mod(tilesPerEdge)
                add(buildTileUrl(template, zoomInt, wrappedC, r, gaode, osm))
                if (gaodeRoad) add(buildRoadUrl(zoomInt, wrappedC, r))
            }
        }
    }
}

@Composable
private fun MapPreviewCard(place: Place?, customUrl: String, activeTileUrl: String, onExpand: () -> Unit) {
    val colors = WeatherTheme.colors
    val lat = place?.latitude ?: 39.886
    val lon = place?.longitude ?: 116.6556

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WeatherTheme.shapes.Card)
            .background(colors.cardBg)
            .border(1.dp, colors.borderBase, WeatherTheme.shapes.Card)
            .bounceClickable(onClick = onExpand)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = stringResource(R.string.map_title), style = WeatherTheme.typography.headlineMedium, color = colors.textPrimary)
                    Text(text = stringResource(R.string.map_tap_hint), fontSize = 12.sp, color = colors.textSecondary)
                }
                Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(WeatherTheme.shapes.Input)
                    .border(1.dp, colors.borderBase, WeatherTheme.shapes.Input)
            ) {
                NativeTileMap(
                    lat = lat,
                    lon = lon,
                    placeName = place?.displayName ?: "",
                    initialZoom = 10f,
                    baseTileUrl = activeTileUrl,
                    customUrl = customUrl,
                    isInteractive = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ExpandedMapOverlay(place: Place?, customUrl: String, activeTileUrl: String, onClose: () -> Unit) {
    val colors = WeatherTheme.colors
    val lat = place?.latitude ?: 39.886
    val lon = place?.longitude ?: 116.6556

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF161616))) {
        NativeTileMap(
            lat = lat,
            lon = lon,
            placeName = place?.displayName ?: "",
            initialZoom = 12f,
            baseTileUrl = activeTileUrl,
            customUrl = customUrl,
            isInteractive = true,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .clip(WeatherTheme.shapes.Pill)
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.borderBase, WeatherTheme.shapes.Pill)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.map_close), tint = colors.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = place?.displayName ?: "", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                Text(text = "${place?.latitude ?: 0.0}, ${place?.longitude ?: 0.0}", fontSize = 11.sp, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(errorType: WeatherErrorType?, onRetry: () -> Unit) {
    val colors = WeatherTheme.colors
    val message = when (errorType) {
        WeatherErrorType.LOCATION -> stringResource(R.string.status_location_denied)
        WeatherErrorType.FETCH -> stringResource(R.string.status_fetch_failed)
        null -> stringResource(R.string.status_no_weather_info)
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(WeatherTheme.shapes.Card).background(colors.cardBg).border(1.dp, colors.borderBase, WeatherTheme.shapes.Card).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
        Text(text = message, color = colors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Box(modifier = Modifier.clip(WeatherTheme.shapes.Pill).background(MaterialTheme.colorScheme.primary).bounceClickable(onClick = onRetry).padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(text = stringResource(R.string.retry), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

private fun resolveWeatherIcon(condition: WeatherCondition): ImageVector = when (condition) {
    WeatherCondition.CLEAR_DAY -> Icons.Default.WbSunny
    WeatherCondition.CLEAR_NIGHT -> Icons.Default.NightsStay
    WeatherCondition.PARTLY_CLOUDY_DAY -> Icons.Default.WbCloudy
    WeatherCondition.PARTLY_CLOUDY_NIGHT -> Icons.Default.NightsStay
    WeatherCondition.CLOUDY -> Icons.Default.Cloud
    WeatherCondition.RAIN -> Icons.Default.WaterDrop
    WeatherCondition.THUNDERSTORM -> Icons.Default.Thunderstorm
    WeatherCondition.SNOW -> Icons.Default.AcUnit
    WeatherCondition.SLEET, WeatherCondition.HAIL -> Icons.Default.Grain
    WeatherCondition.FOG -> Icons.Default.Dehaze
}

private fun formatTemperature(celsius: Double?, unit: TemperatureUnit): String {
    if (celsius == null) return "--"
    val displayValue = if (unit == TemperatureUnit.FAHRENHEIT) {
        (celsius * 9 / 5) + 32
    } else {
        celsius
    }
    return displayValue.toInt().toString()
}

@Composable
private fun shareCardSnapshot(uiState: WeatherUiState): CardSnapshot {
    val current = uiState.weatherData?.current
    val place = uiState.place
    val condition = current?.condition ?: WeatherCondition.CLOUDY
    return CardSnapshot(
        conditionIcon = resolveWeatherIcon(condition),
        placeName = place?.displayName ?: "--",
        placeMeta = listOfNotNull(place?.admin1, place?.country).joinToString(" · "),
        coordsText = String.format(Locale.US, "%.2f°, %.2f°", place?.latitude ?: 0.0, place?.longitude ?: 0.0),
        tempText = formatTemperature(current?.temperature, uiState.temperatureUnit),
        unitText = if (uiState.temperatureUnit == TemperatureUnit.CELSIUS) "°C" else "°F",
        conditionText = stringResource(conditionLabel(condition)),
        updatedText = stringResource(R.string.card_updated, uiState.deviceTimeText),
        humidityLabel = stringResource(R.string.humidity),
        humidityText = current?.humidity?.let { "${it.toInt()}%" } ?: "--",
        windLabel = stringResource(R.string.wind),
        windText = current?.windSpeed?.let { "$it m/s" } ?: "--",
        pressureLabel = stringResource(R.string.pressure),
        pressureText = current?.pressure?.let { "${it.toInt()} hPa" } ?: "--",
        scanHint = stringResource(R.string.card_scan_hint),
        copyright = stringResource(R.string.card_copyright)
    )
}

private fun conditionLabel(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.CLEAR_DAY -> R.string.wx_clear_day
    WeatherCondition.CLEAR_NIGHT -> R.string.wx_clear_night
    WeatherCondition.PARTLY_CLOUDY_DAY -> R.string.wx_partly_day
    WeatherCondition.PARTLY_CLOUDY_NIGHT -> R.string.wx_partly_night
    WeatherCondition.CLOUDY -> R.string.wx_cloudy
    WeatherCondition.RAIN -> R.string.wx_rain
    WeatherCondition.THUNDERSTORM -> R.string.wx_thunder
    WeatherCondition.SNOW -> R.string.wx_snow
    WeatherCondition.SLEET -> R.string.wx_sleet
    WeatherCondition.HAIL -> R.string.wx_hail
    WeatherCondition.FOG -> R.string.wx_fog
}