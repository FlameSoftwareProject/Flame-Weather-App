package app.flameproject.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.flameproject.weather.core.DefaultEndpoints
import app.flameproject.weather.data.local.TemperatureUnit
import app.flameproject.weather.data.local.UserPreferencesRepository
import app.flameproject.weather.data.repository.WeatherRepository
import app.flameproject.weather.domain.location.LocationTracker
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import app.flameproject.weather.ui.locale.apiLanguageCode
import app.flameproject.weather.ui.locale.toLocale
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

data class WeatherUiState(
    val isLoading: Boolean = false,
    val place: Place? = null,
    val weatherData: WeatherData? = null,
    val activeTileUrl: String = DefaultEndpoints.TILE_GAODE,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val searchResults: List<Place> = emptyList(),
    val isSearching: Boolean = false,
    val deviceTimeText: String = "",
    val localTimeText: String = "",
    val errorMessage: String? = null,
    val errorType: WeatherErrorType? = null
)

enum class WeatherErrorType {
    LOCATION,
    FETCH
}

/**
 * Manages weather state, geocoding search, and multi-tier prioritized map tile reachability.
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val probeHttpClient = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val clockLocale = MutableStateFlow(Locale.getDefault())

    init {
        startClockTicker()
        observeClockLocale()
        observePreferences()
        loadInitialLocation()
    }

    private fun observeClockLocale() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                clockLocale.value = prefs.language.toLocale()
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            var cachedLocale: Locale? = null
            var deviceTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            var localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            while (true) {
                val locale = clockLocale.value
                if (locale != cachedLocale) {
                    cachedLocale = locale
                    deviceTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", locale)
                    localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
                }

                val now = ZonedDateTime.now()
                val activePlace = _uiState.value.place
                val localTime = if (activePlace?.timezone != null) {
                    runCatching {
                        now.withZoneSameInstant(java.time.ZoneId.of(activePlace.timezone))
                    }.getOrDefault(now)
                } else now

                _uiState.value = _uiState.value.copy(
                    deviceTimeText = now.format(deviceTimeFormatter),
                    localTimeText = localTime.format(localTimeFormatter)
                )
                delay(1000)
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    temperatureUnit = prefs.temperatureUnit
                )
            }
        }
    }

    private suspend fun probeNetworkTile(customUrl: String): String = withContext(Dispatchers.IO) {
        if (customUrl.isNotBlank() && customUrl.length > 5) {
            return@withContext customUrl
        }

        val googleJob = async {
            withTimeoutOrNull(2000) {
                runCatching {
                    val req = Request.Builder().url("https://mt1.google.com/vt/lyrs=y&x=1&y=0&z=1").head().build()
                    probeHttpClient.newCall(req).execute().use { it.isSuccessful }
                }.getOrDefault(false)
            } ?: false
        }

        val osmJob = async {
            withTimeoutOrNull(2000) {
                runCatching {
                    val req = Request.Builder().url("https://tile.openstreetmap.org/0/0/0.png").head().build()
                    probeHttpClient.newCall(req).execute().use { it.isSuccessful }
                }.getOrDefault(false)
            } ?: false
        }

        val gaodeJob = async {
            withTimeoutOrNull(2000) {
                runCatching {
                    val req = Request.Builder().url("https://webst01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=6&x=6&y=3&z=3").head().build()
                    probeHttpClient.newCall(req).execute().use { it.isSuccessful }
                }.getOrDefault(false)
            } ?: false
        }

        val googleOk = googleJob.await()
        val osmOk = osmJob.await()
        val gaodeOk = gaodeJob.await()

        when {
            googleOk -> DefaultEndpoints.TILE_GOOGLE
            osmOk -> DefaultEndpoints.TILE_OSM
            gaodeOk -> DefaultEndpoints.TILE_GAODE
            else -> DefaultEndpoints.TILE_GAODE
        }
    }

    fun loadInitialLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, errorType = null)
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                val geocodingLanguage = preferencesRepository.userPreferencesFlow.first().language.apiLanguageCode()
                val resolvedPlace = weatherRepository.resolvePlaceName(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    language = geocodingLanguage
                ).copy(isCurrentLocation = true)
                selectPlace(resolvedPlace)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    place = null,
                    weatherData = null,
                    errorMessage = "LOCATION_FAILED",
                    errorType = WeatherErrorType.LOCATION
                )
            }
        }
    }

    fun selectPlace(place: Place) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                place = place,
                isLoading = true,
                errorMessage = null,
                errorType = null,
                searchResults = emptyList(),
                isSearching = false
            )

            launch {
                val custom = preferencesRepository.userPreferencesFlow.first().customMapTileUrl
                val bestTile = probeNetworkTile(custom)
                _uiState.value = _uiState.value.copy(activeTileUrl = bestTile)
            }

            runCatching {
                val weather = weatherRepository.fetchWeather(place)
                _uiState.value = _uiState.value.copy(
                    weatherData = weather,
                    isLoading = false,
                    errorMessage = null,
                    errorType = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weatherData = null,
                    errorMessage = error.localizedMessage ?: "Network connection failed",
                    errorType = WeatherErrorType.FETCH
                )
            }
        }
    }

    fun searchCity(query: String, language: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(isSearching = true)
            val results = weatherRepository.searchCity(query, language)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearching = false
            )
        }
    }

    fun toggleTemperatureUnit() {
        viewModelScope.launch {
            val current = preferencesRepository.userPreferencesFlow.first().temperatureUnit
            val next = if (current == TemperatureUnit.CELSIUS) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
            preferencesRepository.setTemperatureUnit(next)
        }
    }
}