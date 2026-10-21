package app.flameproject.weather.ui.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flameproject.weather.ui.theme.WeatherTheme

data class CardSnapshot(
    val conditionIcon: ImageVector,
    val placeName: String,
    val placeMeta: String,
    val coordsText: String,
    val tempText: String,
    val unitText: String,
    val conditionText: String,
    val updatedText: String,
    val humidityLabel: String,
    val humidityText: String,
    val windLabel: String,
    val windText: String,
    val pressureLabel: String,
    val pressureText: String,
    val scanHint: String,
    val copyright: String
)

/**
 * 4:5 share card rendering the current weather with the app design language.
 */
@Composable
fun ShareCard(
    snapshot: CardSnapshot,
    mosaic: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    val qr = remember { CardExporter.buildQr().asImageBitmap() }
    val heroShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Column(
        modifier = modifier
            .width(360.dp)
            .height(450.dp)
            .clip(WeatherTheme.shapes.Card)
            .background(colors.bgBase)
            .border(1.dp, colors.borderBase, WeatherTheme.shapes.Card)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp)
                .clip(heroShape)
        ) {
            if (mosaic != null) {
                Image(
                    bitmap = mosaic,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to colors.bgBase.copy(alpha = 0.22f),
                            0.55f to colors.bgBase.copy(alpha = 0.62f),
                            1f to colors.bgBase.copy(alpha = 0.96f)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(colors.accentPrimary.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(40f, 40f),
                            radius = 520f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Flame Weather",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = snapshot.coordsText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = snapshot.placeName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 31.sp,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = snapshot.placeMeta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = snapshot.tempText,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-3).sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = snapshot.unitText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 3.dp, bottom = 13.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = snapshot.conditionIcon,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = snapshot.conditionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgBase)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = snapshot.updatedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(
                        icon = Icons.Default.WaterDrop,
                        label = snapshot.humidityLabel,
                        value = snapshot.humidityText,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        icon = Icons.Default.Air,
                        label = snapshot.windLabel,
                        value = snapshot.windText,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        icon = Icons.Default.Compress,
                        label = snapshot.pressureLabel,
                        value = snapshot.pressureText,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = snapshot.scanHint,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "flpr.dev/wt",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = snapshot.copyright,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFAFAFF))
                            .padding(5.dp)
                    ) {
                        Image(
                            bitmap = qr,
                            contentDescription = null,
                            filterQuality = FilterQuality.None,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.borderBase, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
