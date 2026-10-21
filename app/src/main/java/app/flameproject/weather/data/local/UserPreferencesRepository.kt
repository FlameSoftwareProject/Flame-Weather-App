package app.flameproject.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.flameproject.weather.core.DefaultEndpoints
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val PALETTE_SEED = stringPreferencesKey("palette_seed")
        val PALETTE_STYLE = stringPreferencesKey("palette_style")
        val CUSTOM_COLOR_HEX = stringPreferencesKey("custom_color_hex")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")

        val WEATHER_API_URL = stringPreferencesKey("weather_api_url")
        val WEATHER_USER_AGENT = stringPreferencesKey("weather_user_agent")
        val WEATHER_API_KEY = stringPreferencesKey("weather_api_key")

        val CUSTOM_GEOCODING_URL = stringPreferencesKey("custom_geocoding_url")
        val GEOCODING_USER_AGENT = stringPreferencesKey("geocoding_user_agent")
        val GEOCODING_API_KEY = stringPreferencesKey("geocoding_api_key")

        val CUSTOM_MAP_TILE_URL = stringPreferencesKey("custom_map_tile_url")
        val MAP_TILE_USER_AGENT = stringPreferencesKey("map_tile_user_agent")
        val MAP_TILE_API_KEY = stringPreferencesKey("map_tile_api_key")

        val ACTIVE_AI_PROVIDER = stringPreferencesKey("active_ai_provider")
        fun aiKey(provider: String) = stringPreferencesKey("ai_key_$provider")
        fun aiBaseUrl(provider: String) = stringPreferencesKey("ai_base_$provider")
        fun aiModel(provider: String) = stringPreferencesKey("ai_model_$provider")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val darkMode = runCatching {
                DarkModePreference.valueOf(preferences[PreferencesKeys.DARK_MODE] ?: DarkModePreference.SYSTEM.name)
            }.getOrDefault(DarkModePreference.SYSTEM)

            val paletteSeed = resolvePaletteSeed(preferences[PreferencesKeys.PALETTE_SEED])

            val customColorHex = preferences[PreferencesKeys.CUSTOM_COLOR_HEX] ?: "#009688"

            val language = runCatching {
                AppLanguage.valueOf(preferences[PreferencesKeys.APP_LANGUAGE] ?: AppLanguage.SYSTEM.name)
            }.getOrDefault(AppLanguage.SYSTEM)

            val tempUnit = runCatching {
                TemperatureUnit.valueOf(preferences[PreferencesKeys.TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name)
            }.getOrDefault(TemperatureUnit.CELSIUS)

            val paletteStyle = runCatching {
                PaletteStyle.valueOf(preferences[PreferencesKeys.PALETTE_STYLE] ?: PaletteStyle.TONAL_SPOT.name)
            }.getOrDefault(PaletteStyle.TONAL_SPOT)

            val activeAi = runCatching {
                AiProviderType.valueOf(preferences[PreferencesKeys.ACTIVE_AI_PROVIDER] ?: AiProviderType.GOOGLE.name)
            }.getOrDefault(AiProviderType.GOOGLE)

            val providersMap = AiProviderType.entries.associateWith { provider ->
                AiProviderConfig(
                    apiKey = preferences[PreferencesKeys.aiKey(provider.name)] ?: "",
                    baseUrl = preferences[PreferencesKeys.aiBaseUrl(provider.name)] ?: "",
                    model = preferences[PreferencesKeys.aiModel(provider.name)] ?: ""
                )
            }

            UserPreferences(
                darkMode = darkMode,
                pureBlack = preferences[PreferencesKeys.PURE_BLACK] ?: false,
                paletteSeed = paletteSeed,
                paletteStyle = paletteStyle,
                customColorHex = customColorHex,
                language = language,
                temperatureUnit = tempUnit,
                weatherApiUrl = preferences[PreferencesKeys.WEATHER_API_URL] ?: DefaultEndpoints.WEATHER_API,
                weatherUserAgent = preferences[PreferencesKeys.WEATHER_USER_AGENT] ?: "",
                weatherApiKey = preferences[PreferencesKeys.WEATHER_API_KEY] ?: "",
                customGeocodingUrl = preferences[PreferencesKeys.CUSTOM_GEOCODING_URL] ?: "",
                geocodingUserAgent = preferences[PreferencesKeys.GEOCODING_USER_AGENT] ?: "",
                geocodingApiKey = preferences[PreferencesKeys.GEOCODING_API_KEY] ?: "",
                customMapTileUrl = preferences[PreferencesKeys.CUSTOM_MAP_TILE_URL] ?: "",
                mapTileUserAgent = preferences[PreferencesKeys.MAP_TILE_USER_AGENT] ?: "",
                mapTileApiKey = preferences[PreferencesKeys.MAP_TILE_API_KEY] ?: "",
                activeAiProvider = activeAi,
                aiProviders = providersMap
            )
        }

    suspend fun setDarkMode(mode: DarkModePreference) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = mode.name }
    }

    suspend fun setPureBlack(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PURE_BLACK] = enabled }
    }

    suspend fun setPaletteSeed(seed: PaletteSeed) {
        context.dataStore.edit { it[PreferencesKeys.PALETTE_SEED] = seed.name }
    }

    suspend fun setPaletteStyle(style: PaletteStyle) {
        context.dataStore.edit { it[PreferencesKeys.PALETTE_STYLE] = style.name }
    }

    suspend fun setCustomColorHex(hex: String) {
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_COLOR_HEX] = hex.trim() }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[PreferencesKeys.APP_LANGUAGE] = language.name }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[PreferencesKeys.TEMP_UNIT] = unit.name }
    }

    suspend fun setWeatherApiUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.WEATHER_API_URL] = url.trim() }
    }

    suspend fun setWeatherUserAgent(ua: String) {
        context.dataStore.edit { it[PreferencesKeys.WEATHER_USER_AGENT] = ua.trim() }
    }

    suspend fun setWeatherApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.WEATHER_API_KEY] = key.trim() }
    }

    suspend fun setCustomGeocodingUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_GEOCODING_URL] = url.trim() }
    }

    suspend fun setGeocodingUserAgent(ua: String) {
        context.dataStore.edit { it[PreferencesKeys.GEOCODING_USER_AGENT] = ua.trim() }
    }

    suspend fun setGeocodingApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.GEOCODING_API_KEY] = key.trim() }
    }

    suspend fun setCustomMapTileUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_MAP_TILE_URL] = url.trim() }
    }

    suspend fun setMapTileUserAgent(ua: String) {
        context.dataStore.edit { it[PreferencesKeys.MAP_TILE_USER_AGENT] = ua.trim() }
    }

    suspend fun setMapTileApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.MAP_TILE_API_KEY] = key.trim() }
    }

    suspend fun resetApiEndpoints() {
        context.dataStore.edit {
            it[PreferencesKeys.WEATHER_API_URL] = DefaultEndpoints.WEATHER_API
            it[PreferencesKeys.WEATHER_USER_AGENT] = ""
            it[PreferencesKeys.WEATHER_API_KEY] = ""
            it[PreferencesKeys.CUSTOM_GEOCODING_URL] = ""
            it[PreferencesKeys.GEOCODING_USER_AGENT] = ""
            it[PreferencesKeys.GEOCODING_API_KEY] = ""
            it[PreferencesKeys.CUSTOM_MAP_TILE_URL] = ""
            it[PreferencesKeys.MAP_TILE_USER_AGENT] = ""
            it[PreferencesKeys.MAP_TILE_API_KEY] = ""
        }
    }

    suspend fun setActiveAiProvider(provider: AiProviderType) {
        context.dataStore.edit { it[PreferencesKeys.ACTIVE_AI_PROVIDER] = provider.name }
    }

    suspend fun updateAiProviderConfig(provider: AiProviderType, config: AiProviderConfig) {
        context.dataStore.edit {
            it[PreferencesKeys.aiKey(provider.name)] = config.apiKey.trim()
            it[PreferencesKeys.aiBaseUrl(provider.name)] = config.baseUrl.trim()
            it[PreferencesKeys.aiModel(provider.name)] = config.model.trim()
        }
    }
}

private fun resolvePaletteSeed(raw: String?): PaletteSeed =
    raw?.let { name -> runCatching { PaletteSeed.valueOf(name) }.getOrNull() } ?: PaletteSeed.DYNAMIC