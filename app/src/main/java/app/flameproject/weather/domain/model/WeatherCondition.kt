package app.flameproject.weather.domain.model

/**
 * Standardized semantic weather conditions mapped from provider symbol codes.
 */
enum class WeatherCondition {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    RAIN,
    THUNDERSTORM,
    SNOW,
    SLEET,
    HAIL,
    FOG;

    companion object {
        fun fromSymbolCode(code: String?): WeatherCondition {
            if (code == null) return CLOUDY
            val lower = code.lowercase()
            return when {
                lower.contains("thunder") -> THUNDERSTORM
                lower.contains("hail") -> HAIL
                lower.contains("snow") -> SNOW
                lower.contains("sleet") -> SLEET
                lower.contains("rain") || lower.contains("showers") -> RAIN
                lower.contains("fog") -> FOG
                lower.contains("partlycloudy") || lower.contains("fair") -> {
                    if (lower.endsWith("_day")) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
                }
                lower.contains("clearsky") -> {
                    if (lower.endsWith("_day")) CLEAR_DAY else CLEAR_NIGHT
                }
                else -> CLOUDY
            }
        }
    }
}