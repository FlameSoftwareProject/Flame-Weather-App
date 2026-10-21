package app.flameproject.weather.domain.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

/**
 * Robust device location coordinator supporting multi-provider polling, historical cache, and IP fallback.
 */
class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val systemManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    fun isLocationEnabled(): Boolean {
        val manager = systemManager ?: return false
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        // 1. Immediately return freshest cached hardware location
        val bestCached = getFreshestCachedLocation()
        if (bestCached != null && isRecent(bestCached)) {
            return@withContext bestCached
        }

        // 2. Concurrently poll active hardware GPS providers
        val freshGps = withTimeoutOrNull(4500) {
            suspendCancellableCoroutine<Location?> { continuation ->
                var completed = false
                val onLocationReceived = { loc: Location? ->
                    if (!completed && loc != null) {
                        completed = true
                        continuation.resume(loc)
                    }
                }

                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { onLocationReceived(it) }
                    .addOnFailureListener { /* Fallback */ }

                val manager = systemManager
                if (manager != null) {
                    val activeProviders = listOf(
                        LocationManager.GPS_PROVIDER,
                        LocationManager.NETWORK_PROVIDER,
                        LocationManager.PASSIVE_PROVIDER
                    ).filter { manager.isProviderEnabled(it) }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val signal = CancellationSignal()
                        val executor = ContextCompat.getMainExecutor(context)
                        activeProviders.forEach { provider ->
                            runCatching {
                                manager.getCurrentLocation(provider, signal, executor) { loc ->
                                    onLocationReceived(loc)
                                }
                            }
                        }
                    } else {
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                manager.removeUpdates(this)
                                onLocationReceived(location)
                            }
                            override fun onProviderDisabled(provider: String) {}
                            override fun onProviderEnabled(provider: String) {}
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                        }
                        activeProviders.forEach { provider ->
                            runCatching {
                                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                            }
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        }

        if (freshGps != null) return@withContext freshGps
        if (bestCached != null) return@withContext bestCached

        // 3. Fallback to IP geolocation when indoor GPS satellites are unreachable
        fetchIpLocationFallback()
    }

    @SuppressLint("MissingPermission")
    private fun getFreshestCachedLocation(): Location? {
        val manager = systemManager ?: return null
        val candidates = mutableListOf<Location>()

        listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).forEach { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()?.let {
                candidates.add(it)
            }
        }

        return candidates.maxByOrNull { it.time }
    }

    private fun isRecent(location: Location): Boolean {
        return (System.currentTimeMillis() - location.time) < 1000 * 60 * 45
    }

    private fun fetchIpLocationFallback(): Location? {
        return runCatching {
            val url = URL("https://api.bigdatacloud.net/data/reverse-geocode-client")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val lat = obj.optDouble("latitude", Double.NaN)
                val lon = obj.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lon.isNaN()) {
                    Location("ip_provider").apply {
                        latitude = lat
                        longitude = lon
                    }
                } else null
            } else null
        }.getOrNull()
    }
}