package app.flameproject.weather.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Current weather metrics for display in the primary weather card.
 */
data class CurrentWeather(
    val time: Instant,
    val temperature: Double?,
    val humidity: Double?,
    val windSpeed: Double?,
    val pressure: Double?,
    val precipitation: Double?,
    val symbolCode: String?,
    val condition: WeatherCondition
)

/**
 * Forecast point for an individual hour in the 24-hour horizontal forecast.
 */
data class HourlyForecast(
    val time: Instant,
    val temperature: Double?,
    val precipitation: Double?,
    val symbolCode: String?,
    val condition: WeatherCondition
)

/**
 * Aggregated daily forecast metrics for the multi-day forecast list.
 */
data class DailyForecast(
    val date: LocalDate,
    val tempMax: Double?,
    val tempMin: Double?,
    val precipitationTotal: Double?,
    val symbolCode: String?,
    val condition: WeatherCondition
)

/**
 * Complete parsed weather dataset for a given location.
 */
data class WeatherData(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val updatedAt: Instant = Instant.now()
)