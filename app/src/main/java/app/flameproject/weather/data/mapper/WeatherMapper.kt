package app.flameproject.weather.data.mapper

import app.flameproject.weather.data.remote.dto.MetTimeseriesItemDto
import app.flameproject.weather.data.remote.dto.MetWeatherResponseDto
import app.flameproject.weather.domain.model.CurrentWeather
import app.flameproject.weather.domain.model.DailyForecast
import app.flameproject.weather.domain.model.HourlyForecast
import app.flameproject.weather.domain.model.WeatherCondition
import app.flameproject.weather.domain.model.WeatherData
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Transforms raw MET Norway timeseries payloads into domain-level weather structures.
 */
object WeatherMapper {

    private data class ParsedEntry(
        val time: Instant,
        val temp: Double?,
        val humidity: Double?,
        val wind: Double?,
        val pressure: Double?,
        val precip: Double?,
        val symbol: String?
    )

    fun mapToWeatherData(dto: MetWeatherResponseDto, targetZoneId: ZoneId): WeatherData {
        val rawSeries = dto.properties?.timeseries ?: emptyList()
        if (rawSeries.isEmpty()) {
            throw IllegalArgumentException("Empty weather timeseries received from provider")
        }

        val parsedEntries = rawSeries.mapNotNull { parseTimeseriesItem(it) }
        val currentEntry = parsedEntries.first()

        val current = CurrentWeather(
            time = currentEntry.time,
            temperature = currentEntry.temp,
            humidity = currentEntry.humidity,
            windSpeed = currentEntry.wind,
            pressure = currentEntry.pressure,
            precipitation = currentEntry.precip,
            symbolCode = currentEntry.symbol,
            condition = WeatherCondition.fromSymbolCode(currentEntry.symbol)
        )

        val hourlyCutoff = Instant.now().minusSeconds(1800)
        val hourly = parsedEntries
            .filter { it.time.isAfter(hourlyCutoff) }
            .take(24)
            .map {
                HourlyForecast(
                    time = it.time,
                    temperature = it.temp,
                    precipitation = it.precip,
                    symbolCode = it.symbol,
                    condition = WeatherCondition.fromSymbolCode(it.symbol)
                )
            }

        val dailyGroups = parsedEntries.groupBy {
            ZonedDateTime.ofInstant(it.time, targetZoneId).toLocalDate()
        }

        val daily = dailyGroups.entries.take(7).map { (date, entries) ->
            val validTemps = entries.mapNotNull { it.temp }
            val totalPrecip = entries.mapNotNull { it.precip }.takeIf { it.isNotEmpty() }?.sum()

            val middayEntry = entries.find {
                val hour = ZonedDateTime.ofInstant(it.time, targetZoneId).hour
                hour in 11..14
            } ?: entries[entries.size / 2]

            DailyForecast(
                date = date,
                tempMax = validTemps.maxOrNull(),
                tempMin = validTemps.minOrNull(),
                precipitationTotal = totalPrecip,
                symbolCode = middayEntry.symbol,
                condition = WeatherCondition.fromSymbolCode(middayEntry.symbol)
            )
        }

        return WeatherData(
            current = current,
            hourly = hourly,
            daily = daily
        )
    }

    private fun parseTimeseriesItem(item: MetTimeseriesItemDto): ParsedEntry? {
        val time = runCatching { Instant.parse(item.time) }.getOrNull() ?: return null
        val instant = item.data?.instant?.details
        val next1 = item.data?.next1Hours
        val next6 = item.data?.next6Hours
        val next12 = item.data?.next12Hours

        val precip = next1?.details?.precipitationAmount
            ?: next6?.details?.precipitationAmount

        val symbol = next1?.summary?.symbolCode
            ?: next6?.summary?.symbolCode
            ?: next12?.summary?.symbolCode

        return ParsedEntry(
            time = time,
            temp = instant?.airTemperature,
            humidity = instant?.relativeHumidity,
            wind = instant?.windSpeed,
            pressure = instant?.airPressureAtSeaLevel,
            precip = precip,
            symbol = symbol
        )
    }
}