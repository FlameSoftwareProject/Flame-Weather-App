package app.flameproject.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetWeatherResponseDto(
    val properties: MetPropertiesDto? = null
)

@Serializable
data class MetPropertiesDto(
    val timeseries: List<MetTimeseriesItemDto> = emptyList()
)

@Serializable
data class MetTimeseriesItemDto(
    val time: String,
    val data: MetDataDto? = null
)

@Serializable
data class MetDataDto(
    val instant: MetInstantDto? = null,
    @SerialName("next_1_hours") val next1Hours: MetPeriodDto? = null,
    @SerialName("next_6_hours") val next6Hours: MetPeriodDto? = null,
    @SerialName("next_12_hours") val next12Hours: MetPeriodDto? = null
)

@Serializable
data class MetInstantDto(
    val details: MetInstantDetailsDto? = null
)

@Serializable
data class MetInstantDetailsDto(
    @SerialName("air_temperature") val airTemperature: Double? = null,
    @SerialName("relative_humidity") val relativeHumidity: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("air_pressure_at_sea_level") val airPressureAtSeaLevel: Double? = null
)

@Serializable
data class MetPeriodDto(
    val summary: MetSummaryDto? = null,
    val details: MetPeriodDetailsDto? = null
)

@Serializable
data class MetSummaryDto(
    @SerialName("symbol_code") val symbolCode: String? = null
)

@Serializable
data class MetPeriodDetailsDto(
    @SerialName("precipitation_amount") val precipitationAmount: Double? = null
)