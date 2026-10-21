package app.flameproject.weather.domain.model

/**
 * Geographical entity representing a user-selected or device-located place.
 */
data class Place(
    val name: String? = null,
    val admin1: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val isCurrentLocation: Boolean = false
) {
    val displayName: String
        get() {
            val parts = listOfNotNull(name, admin1.takeIf { it != name })
            return if (parts.isNotEmpty()) {
                parts.joinToString(", ")
            } else {
                "%.2f, %.2f".format(latitude, longitude)
            }
        }
}