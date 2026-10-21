package app.flameproject.weather.data.remote

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MapTileClient {

    @Volatile
    private var cached: ImageLoader? = null

    fun loader(context: Context): ImageLoader {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val app = context.applicationContext
            return ImageLoader.Builder(app)
                .memoryCache(MemoryCache.Builder(app).maxSizeBytes(32 * 1024 * 1024).build())
                .diskCache(
                    DiskCache.Builder()
                        .directory(app.cacheDir.resolve("map_tiles"))
                        .maxSizeBytes(128L * 1024 * 1024)
                        .build()
                )
                .crossfade(false)
                .build()
                .also { cached = it }
        }
    }

    suspend fun bitmap(context: Context, url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context.applicationContext)
            .data(url)
            .allowHardware(false)
            .build()
        val result = loader(context).execute(request)
        (result.drawable as? BitmapDrawable)?.bitmap?.asImageBitmap()
    }
}
