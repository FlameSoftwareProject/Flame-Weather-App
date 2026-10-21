package app.flameproject.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.flameproject.weather.data.local.AppLanguage
import app.flameproject.weather.data.local.DarkModePreference
import app.flameproject.weather.data.local.PaletteSeed
import app.flameproject.weather.data.local.PaletteStyle
import app.flameproject.weather.data.local.UserPreferences
import app.flameproject.weather.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Manages configuration updates for colors, languages, and the 3 core API endpoints.
 */
class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setDarkMode(mode: DarkModePreference) {
        viewModelScope.launch { preferencesRepository.setDarkMode(mode) }
    }

    fun setPureBlack(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setPureBlack(enabled) }
    }

    fun setPaletteSeed(seed: PaletteSeed) {
        viewModelScope.launch { preferencesRepository.setPaletteSeed(seed) }
    }

    fun setPaletteStyle(style: PaletteStyle) {
        viewModelScope.launch { preferencesRepository.setPaletteStyle(style) }
    }

    fun setCustomColorHex(hex: String) {
        viewModelScope.launch { preferencesRepository.setCustomColorHex(hex) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesRepository.setLanguage(language) }
    }

    fun setWeatherApiUrl(url: String) {
        viewModelScope.launch { preferencesRepository.setWeatherApiUrl(url) }
    }
    fun setWeatherUserAgent(ua: String) {
        viewModelScope.launch { preferencesRepository.setWeatherUserAgent(ua) }
    }
    fun setWeatherApiKey(key: String) {
        viewModelScope.launch { preferencesRepository.setWeatherApiKey(key) }
    }

    fun setCustomGeocodingUrl(url: String) {
        viewModelScope.launch { preferencesRepository.setCustomGeocodingUrl(url) }
    }
    fun setGeocodingUserAgent(ua: String) {
        viewModelScope.launch { preferencesRepository.setGeocodingUserAgent(ua) }
    }
    fun setGeocodingApiKey(key: String) {
        viewModelScope.launch { preferencesRepository.setGeocodingApiKey(key) }
    }

    fun setCustomMapTileUrl(url: String) {
        viewModelScope.launch { preferencesRepository.setCustomMapTileUrl(url) }
    }
    fun setMapTileUserAgent(ua: String) {
        viewModelScope.launch { preferencesRepository.setMapTileUserAgent(ua) }
    }
    fun setMapTileApiKey(key: String) {
        viewModelScope.launch { preferencesRepository.setMapTileApiKey(key) }
    }

    fun resetApiEndpoints() {
        viewModelScope.launch { preferencesRepository.resetApiEndpoints() }
    }
}