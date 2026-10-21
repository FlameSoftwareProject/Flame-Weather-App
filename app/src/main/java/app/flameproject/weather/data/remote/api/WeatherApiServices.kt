package app.flameproject.weather.data.remote.api

import app.flameproject.weather.data.remote.dto.BigDataCloudReverseResponseDto
import app.flameproject.weather.data.remote.dto.MetWeatherResponseDto
import app.flameproject.weather.data.remote.dto.NominatimResultDto
import app.flameproject.weather.data.remote.dto.OpenMeteoSearchResponseDto
import app.flameproject.weather.data.remote.dto.PhotonResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Weather endpoint providing MET Norway timeseries payloads with dynamic headers.
 */
interface WeatherApiService {
    @GET
    suspend fun fetchWeather(
        @Url fullUrl: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Header("User-Agent") userAgent: String,
        @Header("Authorization") authHeader: String?,
        @Header("X-Api-Key") apiKeyHeader: String?,
        @Header("Accept") acceptHeader: String = "application/json"
    ): MetWeatherResponseDto
}

/**
 * Multi-provider geocoding and reverse-geocoding endpoints with dynamic headers.
 */
interface GeocodingApiService {
    @GET
    suspend fun searchOpenMeteo(
        @Url url: String,
        @Query("name") query: String,
        @Query("count") count: Int = 30,
        @Query("language") language: String,
        @Query("format") format: String = "json",
        @Header("User-Agent") userAgent: String,
        @Header("Authorization") authHeader: String?,
        @Header("X-Api-Key") apiKeyHeader: String?
    ): OpenMeteoSearchResponseDto

    @GET
    suspend fun searchNominatim(
        @Url url: String,
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") language: String,
        @Query("limit") limit: Int = 10,
        @Header("User-Agent") userAgent: String,
        @Header("Authorization") authHeader: String?,
        @Header("X-Api-Key") apiKeyHeader: String?
    ): List<NominatimResultDto>

    @GET
    suspend fun searchPhoton(
        @Url url: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Header("User-Agent") userAgent: String,
        @Header("Authorization") authHeader: String?,
        @Header("X-Api-Key") apiKeyHeader: String?
    ): PhotonResponseDto

    @GET
    suspend fun reverseGeocode(
        @Url url: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") language: String,
        @Header("User-Agent") userAgent: String,
        @Header("Authorization") authHeader: String?,
        @Header("X-Api-Key") apiKeyHeader: String?
    ): BigDataCloudReverseResponseDto
}