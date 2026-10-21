package app.flameproject.weather.data.repository

import app.flameproject.weather.core.DefaultEndpoints
import app.flameproject.weather.data.local.UserPreferencesRepository
import app.flameproject.weather.data.mapper.WeatherMapper
import app.flameproject.weather.data.remote.NetworkClient
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.util.Locale

/**
 * Coordinates remote weather fetching and geocoding with strict fallback override policies.
 */
class WeatherRepository(
    private val preferencesRepository: UserPreferencesRepository
) {
    private val weatherApi = NetworkClient.weatherApi
    private val geocodingApi = NetworkClient.geocodingApi

    private fun normalizeCoordinate(coord: Double): Double {
        return String.format(Locale.US, "%.4f", coord).toDouble()
    }

    private fun resolveUserAgent(customUa: String): String {
        return customUa.takeIf { it.isNotBlank() } ?: "WeatherApp/1.0.0"
    }

    suspend fun fetchWeather(place: Place): WeatherData {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val zoneId = place.timezone?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: ZoneId.systemDefault()

        val cleanLat = normalizeCoordinate(place.latitude)
        val cleanLon = normalizeCoordinate(place.longitude)

        val ua = resolveUserAgent(prefs.weatherUserAgent)
        val key = prefs.weatherApiKey.takeIf { it.isNotBlank() }
        val finalUrl = prefs.weatherApiUrl.replace("{key}", key ?: "")

        val responseDto = weatherApi.fetchWeather(
            fullUrl = finalUrl,
            latitude = cleanLat,
            longitude = cleanLon,
            userAgent = ua,
            authHeader = key?.let { "Bearer $it" },
            apiKeyHeader = key
        )

        return WeatherMapper.mapToWeatherData(responseDto, zoneId)
    }

    suspend fun searchCity(query: String, language: String): List<Place> {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < 2) return emptyList()

        val ua = resolveUserAgent(prefs.geocodingUserAgent)
        val key = prefs.geocodingApiKey.takeIf { it.isNotBlank() }
        val authHeader = key?.let { "Bearer $it" }

        // 1. Strict user override: NO FALLBACK if custom URL is configured
        if (prefs.customGeocodingUrl.isNotBlank()) {
            val finalUrl = prefs.customGeocodingUrl.replace("{key}", key ?: "")
            return runCatching {
                geocodingApi.searchOpenMeteo(
                    url = finalUrl, query = normalizedQuery, language = language,
                    userAgent = ua, authHeader = authHeader, apiKeyHeader = key
                ).results.map {
                    Place(
                        name = it.name, admin1 = it.admin1, country = it.country,
                        countryCode = it.countryCode, latitude = normalizeCoordinate(it.latitude),
                        longitude = normalizeCoordinate(it.longitude), timezone = it.timezone
                    )
                }
            }.getOrDefault(emptyList())
        }

        // 2. Default cascading fallback chain: Open-Meteo -> Nominatim -> Photon
        val openMeteoResults = runCatching {
            geocodingApi.searchOpenMeteo(
                url = DefaultEndpoints.GEOCODING_OPEN_METEO, query = normalizedQuery, language = language,
                userAgent = ua, authHeader = null, apiKeyHeader = null
            ).results.map {
                Place(
                    name = it.name, admin1 = it.admin1, country = it.country,
                    countryCode = it.countryCode, latitude = normalizeCoordinate(it.latitude),
                    longitude = normalizeCoordinate(it.longitude), timezone = it.timezone
                )
            }
        }.getOrDefault(emptyList())

        if (openMeteoResults.isNotEmpty()) return openMeteoResults

        val nominatimResults = runCatching {
            geocodingApi.searchNominatim(
                url = DefaultEndpoints.GEOCODING_NOMINATIM, query = normalizedQuery, language = language,
                userAgent = ua, authHeader = null, apiKeyHeader = null
            ).mapNotNull { item ->
                val lat = item.lat.toDoubleOrNull() ?: return@mapNotNull null
                val lon = item.lon.toDoubleOrNull() ?: return@mapNotNull null
                Place(
                    name = item.name ?: item.displayName?.split(",")?.firstOrNull(),
                    admin1 = item.address?.state ?: item.address?.county,
                    country = item.address?.country, countryCode = item.address?.countryCode,
                    latitude = normalizeCoordinate(lat), longitude = normalizeCoordinate(lon)
                )
            }
        }.getOrDefault(emptyList())

        if (nominatimResults.isNotEmpty()) return nominatimResults

        return runCatching {
            geocodingApi.searchPhoton(
                url = DefaultEndpoints.GEOCODING_PHOTON, query = normalizedQuery,
                userAgent = ua, authHeader = null, apiKeyHeader = null
            ).features.mapNotNull { feature ->
                val coords = feature.geometry?.coordinates ?: return@mapNotNull null
                if (coords.size < 2) return@mapNotNull null
                val props = feature.properties
                Place(
                    name = props?.name, admin1 = props?.state, country = props?.country,
                    countryCode = props?.countrycode, latitude = normalizeCoordinate(coords[1]),
                    longitude = normalizeCoordinate(coords[0])
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun resolvePlaceName(latitude: Double, longitude: Double, language: String): Place {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val cleanLat = normalizeCoordinate(latitude)
        val cleanLon = normalizeCoordinate(longitude)
        val ua = resolveUserAgent(prefs.geocodingUserAgent)

        return runCatching {
            val response = geocodingApi.reverseGeocode(
                url = DefaultEndpoints.REVERSE_GEOCODE_BIGDATA,
                latitude = cleanLat, longitude = cleanLon, language = language,
                userAgent = ua, authHeader = null, apiKeyHeader = null
            )

            Place(
                name = response.locality ?: response.city,
                admin1 = if (response.city != response.locality) response.city else response.principalSubdivision,
                country = response.countryName,
                countryCode = response.countryCode,
                latitude = cleanLat,
                longitude = cleanLon
            )
        }.getOrElse {
            Place(latitude = cleanLat, longitude = cleanLon)
        }
    }
}