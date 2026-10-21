package app.flameproject.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Open-Meteo Geocoding
@Serializable
data class OpenMeteoSearchResponseDto(
    val results: List<OpenMeteoResultDto> = emptyList()
)

@Serializable
data class OpenMeteoResultDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("country_code") val countryCode: String? = null,
    val country: String? = null,
    val admin1: String? = null,
    val timezone: String? = null,
    @SerialName("feature_code") val featureCode: String? = null,
    val population: Long? = null
)

// OpenStreetMap Nominatim
@Serializable
data class NominatimResultDto(
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val lat: String,
    val lon: String,
    val category: String? = null,
    val type: String? = null,
    val address: NominatimAddressDto? = null
)

@Serializable
data class NominatimAddressDto(
    val state: String? = null,
    val county: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null
)

// Photon (Komoot)
@Serializable
data class PhotonResponseDto(
    val features: List<PhotonFeatureDto> = emptyList()
)

@Serializable
data class PhotonFeatureDto(
    val geometry: PhotonGeometryDto? = null,
    val properties: PhotonPropertiesDto? = null
)

@Serializable
data class PhotonGeometryDto(
    val coordinates: List<Double> = emptyList()
)

@Serializable
data class PhotonPropertiesDto(
    val name: String? = null,
    val state: String? = null,
    val country: String? = null,
    val countrycode: String? = null,
    @SerialName("osm_value") val osmValue: String? = null
)

// BigDataCloud Reverse Geocoding
@Serializable
data class BigDataCloudReverseResponseDto(
    val locality: String? = null,
    val city: String? = null,
    val principalSubdivision: String? = null,
    val countryName: String? = null,
    val countryCode: String? = null
)