package app.flameproject.weather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flameproject.weather.R
import app.flameproject.weather.ui.locale.aiReplyLanguageName
import app.flameproject.weather.ui.locale.apiLanguageCode
import app.flameproject.weather.ui.screens.ai.AiScreen
import app.flameproject.weather.ui.screens.home.HomeScreen
import app.flameproject.weather.ui.screens.settings.SettingsScreen
import app.flameproject.weather.ui.viewmodel.AiViewModel
import app.flameproject.weather.ui.viewmodel.SettingsViewModel
import app.flameproject.weather.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

/**
 * Top-level application shell coordinating navigation, state propagation, and screen transitions.
 */
@Composable
fun MainScreen(
    weatherViewModel: WeatherViewModel,
    aiViewModel: AiViewModel,
    settingsViewModel: SettingsViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val weatherUiState by weatherViewModel.uiState.collectAsState()
    val aiUiState by aiViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.preferences.collectAsState()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppNavigationBarItem(
                        icon = Icons.Default.Cloud,
                        label = stringResource(R.string.nav_weather),
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        modifier = Modifier.weight(1f)
                    )
                    AppNavigationBarItem(
                        icon = Icons.Default.AutoAwesome,
                        label = stringResource(R.string.nav_ai),
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        modifier = Modifier.weight(1f)
                    )
                    AppNavigationBarItem(
                        icon = Icons.Default.Settings,
                        label = stringResource(R.string.nav_settings),
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                0 -> HomeScreen(
                    uiState = weatherUiState,
                    customMapTileUrl = settingsState.customMapTileUrl,
                    onSearchQueryChanged = { weatherViewModel.searchCity(it, settingsState.language.apiLanguageCode()) },
                    onPlaceSelected = { weatherViewModel.selectPlace(it) },
                    onLocateClicked = { weatherViewModel.loadInitialLocation() },
                    onUnitToggleClicked = { weatherViewModel.toggleTemperatureUnit() }
                )
                1 -> AiScreen(
                    aiState = aiUiState,
                    currentPlace = weatherUiState.place,
                    weatherData = weatherUiState.weatherData,
                    onProviderSelected = { aiViewModel.selectProvider(it) },
                    onConfigUpdated = { provider, k, b, m -> aiViewModel.updateConfig(provider, k, b, m) },
                    onTestConnection = { aiViewModel.testConnection() },
                    onRunAnalysis = {
                        aiViewModel.runAnalysis(
                            weatherUiState.place,
                            weatherUiState.weatherData,
                            settingsState.language.aiReplyLanguageName()
                        )
                    }
                )
                2 -> SettingsScreen(
                    preferences = settingsState,
                    onDarkModeChanged = { settingsViewModel.setDarkMode(it) },
                    onPureBlackChanged = { settingsViewModel.setPureBlack(it) },
                    onPaletteSeedChanged = { settingsViewModel.setPaletteSeed(it) },
                    onPaletteStyleChanged = { settingsViewModel.setPaletteStyle(it) },
                    onCustomColorHexChanged = { settingsViewModel.setCustomColorHex(it) },
                    onLanguageChanged = { settingsViewModel.setLanguage(it) },
                    onWeatherApiUrlChanged = { settingsViewModel.setWeatherApiUrl(it) },
                    onWeatherUserAgentChanged = { settingsViewModel.setWeatherUserAgent(it) },
                    onWeatherApiKeyChanged = { settingsViewModel.setWeatherApiKey(it) },
                    onGeocodingUrlChanged = { settingsViewModel.setCustomGeocodingUrl(it) },
                    onGeocodingUserAgentChanged = { settingsViewModel.setGeocodingUserAgent(it) },
                    onGeocodingApiKeyChanged = { settingsViewModel.setGeocodingApiKey(it) },
                    onMapTileUrlChanged = { settingsViewModel.setCustomMapTileUrl(it) },
                    onMapTileUserAgentChanged = { settingsViewModel.setMapTileUserAgent(it) },
                    onMapTileApiKeyChanged = { settingsViewModel.setMapTileApiKey(it) },
                    onResetEndpoints = { settingsViewModel.resetApiEndpoints() }
                )
            }
        }
    }
}

@Composable
private fun AppNavigationBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}