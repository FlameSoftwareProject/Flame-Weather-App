package app.flameproject.weather.core

/**
 * Default network endpoints used across weather, geocoding, tile, and auxiliary services.
 */
object DefaultEndpoints {
    const val WEATHER_API = "https://weather.flameapis.com/"
    const val GEOCODING_OPEN_METEO = "https://geocoding-api.open-meteo.com/v1/search"
    const val GEOCODING_NOMINATIM = "https://nominatim.openstreetmap.org/search"
    const val GEOCODING_PHOTON = "https://photon.komoot.io/api/"
    const val REVERSE_GEOCODE_BIGDATA = "https://api.bigdatacloud.net/data/reverse-geocode-client"
    const val TILE_GOOGLE = "https://mt{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}"
    const val TILE_OSM = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    const val TILE_GAODE = "https://webst0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=6&x={x}&y={y}&z={z}"
}