package app.flameproject.weather.ui.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.RectF
import androidx.core.content.FileProvider
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.flameproject.weather.data.remote.MapTileClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.tan

const val CARD_SHARE_URL = "https://flameproject.app/weather"

/**
 * Offline rendering pipeline for the share card: map mosaic, QR image and PNG export.
 */
object CardExporter {

    suspend fun buildMosaic(
        context: Context,
        latitude: Double,
        longitude: Double,
        zoom: Int,
        width: Int,
        height: Int,
        tileSize: Double,
        tileUrlTemplate: String
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val tilesPerEdge = 1 shl zoom
        val worldX = mercatorFractionX(longitude)
        val worldY = mercatorFractionY(latitude)
        val worldPixel = tileSize * 2.0.pow(zoom.toDouble())
        val minMercatorX = worldX - width / 2.0 / worldPixel
        val maxMercatorX = worldX + width / 2.0 / worldPixel
        val minMercatorY = worldY - height / 2.0 / worldPixel
        val maxMercatorY = worldY + height / 2.0 / worldPixel

        val startCol = floor(minMercatorX * tilesPerEdge).toInt() - 1
        val endCol = floor(maxMercatorX * tilesPerEdge).toInt() + 1
        val startRow = floor(minMercatorY * tilesPerEdge).toInt() - 1
        val endRow = floor(maxMercatorY * tilesPerEdge).toInt() + 1

        val gaode = tileUrlTemplate.contains("autonavi")
        val osm = tileUrlTemplate.contains("openstreetmap")

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(AndroidColor.rgb(0x10, 0x10, 0x18))
        val patch = RectF()
        var loaded = 0

        val requests = buildList {
            for (row in startRow..endRow) {
                if (row < 0 || row >= tilesPerEdge) continue
                for (col in startCol..endCol) {
                    val wrappedCol = col.mod(tilesPerEdge)
                    val url = tileUrl(tileUrlTemplate, zoom, wrappedCol, row, gaode, osm)
                    val left = (col / tilesPerEdge.toDouble() - worldX) * worldPixel + width / 2.0
                    val top = (row / tilesPerEdge.toDouble() - worldY) * worldPixel + height / 2.0
                    add(TileRequest(url, left.toFloat(), top.toFloat()))
                }
            }
        }

        coroutineScope {
            val gate = Semaphore(4)
            val jobs = requests.map { request ->
                async {
                    gate.withPermit {
                        runCatching {
                            MapTileClient.bitmap(context, request.url)
                        }.getOrNull()
                    }
                }
            }
            val bitmaps = jobs.awaitAll()
            bitmaps.forEachIndexed { index, bitmap ->
                val request = requests[index]
                if (bitmap != null) {
                    patch.set(request.left, request.top, request.left + tileSize.toFloat(), request.top + tileSize.toFloat())
                    canvas.drawBitmap(bitmap.asAndroidBitmap(), null, patch, null)
                    loaded++
                }
            }
        }

        if (loaded == 0) {
            output.recycle()
            null
        } else {
            output.asImageBitmap()
        }
    }

    fun buildQr(): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(CARD_SHARE_URL, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (matrix.get(x, y)) {
                    AndroidColor.rgb(0x17, 0x15, 0x1E)
                } else {
                    AndroidColor.WHITE
                }
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    suspend fun saveAndShare(context: Context, image: ImageBitmap, fileName: String): Boolean {
        return try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportsDir, fileName)
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { stream ->
                    image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(fileName, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(share, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (_: Throwable) {
            false
        }
    }

    private data class TileRequest(val url: String, val left: Float, val top: Float)

    private fun tileUrl(
        template: String,
        zoom: Int,
        column: Int,
        row: Int,
        gaode: Boolean,
        osm: Boolean
    ): String {
        val subdomain = when {
            osm -> arrayOf("a", "b", "c")[abs(column + row) % 3]
            gaode -> ((abs(column + row) % 4) + 1).toString()
            else -> (abs(column + row) % 4).toString()
        }
        return template.replace("{x}", column.toString())
            .replace("{y}", row.toString())
            .replace("{z}", zoom.toString())
            .replace("{s}", subdomain)
    }

    private fun mercatorFractionX(longitude: Double): Double = (longitude + 180.0) / 360.0

    private fun mercatorFractionY(latitude: Double): Double {
        val latRad = latitude * PI / 180.0
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0
    }
}
