package app.flameproject.weather.data.local

import app.flameproject.weather.core.DefaultEndpoints

enum class DarkModePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class PaletteSeed {
    DYNAMIC,
    LAVENDER,
    MINT,
    MATCHA,
    MARIGOLD,
    ROSE,
    CUSTOM
}

enum class AppLanguage(val code: String) {
    SYSTEM(""),
    EN("en"),
    ZH_HANS("zh-Hans"),
    ZH_HANT("zh-Hant"),
    JA("ja"),
    KO("ko"),
    RU("ru"),
    ES("es")
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class AiProviderType {
    GOOGLE,
    OPENAI,
    ANTHROPIC,
    DEEPSEEK,
    ZHIPU,
    QWEN,
    OPENROUTER,
    CUSTOM
}

enum class PaletteStyle {
    TONAL_SPOT,
    FIDELITY
}

data class AiProviderConfig(
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = ""
)

data class UserPreferences(
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val pureBlack: Boolean = false,
    val paletteSeed: PaletteSeed = PaletteSeed.DYNAMIC,
    val paletteStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
    val customColorHex: String = "#009688",
    val language: AppLanguage = AppLanguage.SYSTEM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,

    // 1. Weather API
    val weatherApiUrl: String = DefaultEndpoints.WEATHER_API,
    val weatherUserAgent: String = "",
    val weatherApiKey: String = "",

    // 2. Geocoding API (Empty = default cascade; Non-empty = custom without fallback)
    val customGeocodingUrl: String = "",
    val geocodingUserAgent: String = "",
    val geocodingApiKey: String = "",

    // 3. Map Tile API (Empty = Google->OSM cascade; Non-empty = custom without fallback)
    val customMapTileUrl: String = "",
    val mapTileUserAgent: String = "",
    val mapTileApiKey: String = "",

    val activeAiProvider: AiProviderType = AiProviderType.GOOGLE,
    val aiProviders: Map<AiProviderType, AiProviderConfig> = emptyMap()
)