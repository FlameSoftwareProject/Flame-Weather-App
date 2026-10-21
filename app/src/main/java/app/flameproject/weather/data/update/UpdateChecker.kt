package app.flameproject.weather.data.update

import app.flameproject.weather.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.Request
import java.io.IOException

/** Latest published GitHub release. */
data class Release(val tagName: String, val htmlUrl: String)

/** GitHub Releases update probe. */
object UpdateChecker {

    private const val RELEASES_LATEST_URL =
        "https://api.github.com/repos/FlameSoftwareProject/Flame-Weather-App/releases/latest"

    /**
     * Fetch the newest published release.
     * @return null when the repository has no release yet.
     * @throws java.io.IOException on network or HTTP failure.
     */
    suspend fun fetchLatestRelease(): Release? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(RELEASES_LATEST_URL)
            .header("Accept", "application/vnd.github+json")
            .build()
        NetworkClient.okHttpClient.newCall(request).execute().use { response ->
            when {
                response.code == 404 -> null
                !response.isSuccessful -> throw IOException("HTTP ${response.code}")
                else -> {
                    val payload = NetworkClient.json.decodeFromString<GitHubRelease>(response.body?.string().orEmpty())
                    Release(payload.tagName, payload.htmlUrl)
                }
            }
        }
    }

    /** @return true when latestTag denotes a version newer than current. */
    fun isNewer(latestTag: String, current: String): Boolean {
        val latest = latestTag.trim().trimStart('v', 'V')
        val left = latest.split('.').map { it.toIntOrNull() }
        val right = current.split('.').map { it.toIntOrNull() }
        if (left.any { it == null } || right.any { it == null }) return latest != current
        for (i in 0 until maxOf(left.size, right.size)) {
            val a = left.getOrElse(i) { 0 }!!
            val b = right.getOrElse(i) { 0 }!!
            if (a != b) return a > b
        }
        return false
    }
}

@Serializable
private data class GitHubRelease(val tagName: String, val htmlUrl: String)
