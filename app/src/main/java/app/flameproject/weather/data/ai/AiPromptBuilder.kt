package app.flameproject.weather.data.ai

import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Prepares system instructions and structured weather payloads for model consumption.
 */
object AiPromptBuilder {

    const val SYSTEM_PROMPT = """You are the weather analysis engine inside a modern consumer weather application. You receive structured weather data (current conditions, hourly and daily forecast) as JSON and produce a short, practical analysis for the end user.

Output rules (strict):
- Respond with a single JSON object only — no markdown code fences, no text before or after it.
- The JSON must match exactly this structure:
  {"basic": {"temperature": string, "humidity": string, "wind": string, "pressure": string, "precipitation": string}, "advice": {"clothing": string, "sun": string, "outdoor": string, "carWash": string, "umbrella": string}}
- "basic" states the current conditions: how hot or cold it feels (mention the value), how humid the air is, how strong the wind is, whether pressure is high or low, and the precipitation situation.
- "advice" turns the data into decisions: what to wear (clothing); whether sun protection or extra warmth matters more today (sun); which outdoor activities are suitable today, or whether one should avoid staying outside (outdoor); whether today is a good day to wash a car, considering whether upcoming rain would soil it again (carWash); whether one should carry an umbrella when going out (umbrella).
- The user message carries three clocks: "local_time" is the current local time at the analysed location, "device_time" is the phone's own clock, and "utc_time" is the absolute instant. Trust "local_time" when judging the time of day at the location.
- The user message also carries "hourly_forecast_next_12h" and "daily_forecast_next_days". Ground every forward-looking statement in that forecast. If the local time is 15:00 (3 PM) or later, tomorrow deserves equal weight in the advice: whenever tomorrow's weather would change the recommendation, mention it explicitly in clothing, umbrella and outdoor entries. Before 15:00, focus on the remainder of today.
- Every string must be written in the language named in the "reply_language" field of the user message. Keep each string to one short sentence, under 40 words.
- Ground every statement in the provided data only. Never invent numbers; if a piece of data is missing, answer qualitatively or say it is unavailable.
- No greetings, no self-introduction, no meta commentary — the JSON content only."""

    fun buildUserText(
        replyLanguage: String,
        place: Place,
        weather: WeatherData,
        unitSymbol: String,
        locale: Locale
    ): String {
        val deviceNow = ZonedDateTime.now()
        val placeZone = place.timezone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: deviceNow.zone
        val placeNow = deviceNow.withZoneSameInstant(placeZone)

        val dateTimeFormat = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy HH:mm", locale)
        val clockFormat = DateTimeFormatter.ofPattern("HH:mm:ss", locale)
        val hourLabelFormat = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val dayLabelFormat = DateTimeFormatter.ofPattern("MM-dd")

        val currentJson = buildJsonObject {
            put("temperature", weather.current.temperature ?: 0.0)
            put("humidity", weather.current.humidity ?: 0.0)
            put("wind_speed", weather.current.windSpeed ?: 0.0)
            put("pressure", weather.current.pressure ?: 0.0)
            put("precipitation", weather.current.precipitation ?: 0.0)
            put("condition", weather.current.condition.name)
        }.toString()

        val hourlyJson = buildJsonArray {
            weather.hourly.take(12).forEach { hour ->
                add(
                    buildJsonObject {
                        put("time", hour.time.atZone(placeZone).format(hourLabelFormat))
                        put("temperature", hour.temperature ?: 0.0)
                        put("precipitation", hour.precipitation ?: 0.0)
                        put("condition", hour.condition.name)
                    }
                )
            }
        }.toString()

        val dailyJson = buildJsonArray {
            weather.daily.take(3).forEach { day ->
                add(
                    buildJsonObject {
                        put("date", day.date.format(dayLabelFormat))
                        put("temp_max", day.tempMax ?: 0.0)
                        put("temp_min", day.tempMin ?: 0.0)
                        put("precipitation_total", day.precipitationTotal ?: 0.0)
                        put("condition", day.condition.name)
                    }
                )
            }
        }.toString()

        return """
            reply_language: $replyLanguage
            device_time: ${deviceNow.format(dateTimeFormat)} ${deviceNow.format(clockFormat)} (${deviceNow.zone.id}, ${deviceNow.offset.id})
            local_time: ${placeNow.format(dateTimeFormat)} ${placeNow.format(clockFormat)} (${placeNow.zone.id}, ${placeNow.offset.id})
            utc_time: ${deviceNow.toInstant()}
            location: ${place.displayName}
            timezone: ${placeZone.id}
            units: $unitSymbol
            current_weather:
            $currentJson
            hourly_forecast_next_12h:
            $hourlyJson
            daily_forecast_next_days:
            $dailyJson
        """.trimIndent()
    }
}
