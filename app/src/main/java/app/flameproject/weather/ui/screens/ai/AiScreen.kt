package app.flameproject.weather.ui.screens.ai

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flameproject.weather.R
import app.flameproject.weather.data.ai.AiProviderDefaults
import app.flameproject.weather.data.local.AiProviderType
import app.flameproject.weather.domain.model.AiErrorKeys
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import app.flameproject.weather.ui.motion.bounceClickable
import app.flameproject.weather.ui.theme.WeatherTheme
import app.flameproject.weather.ui.viewmodel.AiUiState

/**
 * AI weather analysis dashboard with persistent provider configuration and direct analysis execution.
 */
@Composable
fun AiScreen(
    aiState: AiUiState,
    currentPlace: Place?,
    weatherData: WeatherData?,
    onProviderSelected: (AiProviderType) -> Unit,
    onConfigUpdated: (AiProviderType, String, String, String) -> Unit,
    onTestConnection: () -> Unit,
    onRunAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProvider = aiState.activeProvider
    var draftApiKey by remember { mutableStateOf(aiState.config.apiKey) }
    var draftBaseUrl by remember { mutableStateOf(aiState.config.baseUrl) }
    var draftModel by remember { mutableStateOf(aiState.config.model) }
    var isKeyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(activeProvider) {
        draftApiKey = aiState.config.apiKey
        draftBaseUrl = aiState.config.baseUrl
        draftModel = aiState.config.model
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = WeatherTheme.shapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.label_location_format,
                                currentPlace?.displayName ?: "--"
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${weatherData?.current?.temperature?.toInt() ?: "--"}°C",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .bounceClickable(onClick = onRunAnalysis)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (aiState.isRunning) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.ai_run_analysis),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        aiState.errorMessage?.let { message ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = WeatherTheme.shapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ai_error_title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        val detail: String? = when (message) {
                            AiErrorKeys.MISSING_API_KEY -> stringResource(R.string.ai_error_missing_api_key)
                            AiErrorKeys.CONNECTION_FAILED -> stringResource(R.string.ai_error_connection_failed)
                            AiErrorKeys.UNPARSEABLE -> stringResource(R.string.ai_error_unparseable)
                            AiErrorKeys.ANALYSIS_FAILED -> null
                            else -> message
                        }
                        if (detail != null) {
                            Text(
                                text = detail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        val advice = aiState.analysisResult?.advice
        val streaming = aiState.streamingAdvice
        val clothingText = advice?.clothing ?: streaming["clothing"].orEmpty()
        val sunText = advice?.sun ?: streaming["sun"].orEmpty()
        val outdoorText = advice?.outdoor ?: streaming["outdoor"].orEmpty()
        val carWashText = advice?.carWash ?: streaming["carWash"].orEmpty()
        val umbrellaText = advice?.umbrella ?: streaming["umbrella"].orEmpty()

        if (clothingText.isNotBlank() || sunText.isNotBlank() || outdoorText.isNotBlank() ||
            carWashText.isNotBlank() || umbrellaText.isNotBlank()
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (clothingText.isNotBlank()) {
                        AdviceCard(icon = Icons.Default.Checkroom, title = stringResource(R.string.ai_advice_clothing), text = clothingText)
                    }
                    if (sunText.isNotBlank()) {
                        AdviceCard(icon = Icons.Default.WbSunny, title = stringResource(R.string.ai_advice_sun), text = sunText)
                    }
                    if (outdoorText.isNotBlank()) {
                        AdviceCard(icon = Icons.Default.Hiking, title = stringResource(R.string.ai_advice_outdoor), text = outdoorText)
                    }
                    if (carWashText.isNotBlank()) {
                        AdviceCard(icon = Icons.Default.DirectionsCar, title = stringResource(R.string.ai_advice_car_wash), text = carWashText)
                    }
                    if (umbrellaText.isNotBlank()) {
                        AdviceCard(icon = Icons.Default.Umbrella, title = stringResource(R.string.ai_advice_umbrella), text = umbrellaText)
                    }
                }
            }
        }

        if (aiState.isRunning) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (aiState.retryAttempt > 0) {
                            stringResource(R.string.ai_retrying_status)
                        } else {
                            stringResource(R.string.ai_streaming_status)
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = WeatherTheme.shapes.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.ai_privacy_guarantee),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.ai_label_provider),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        val providers = listOf(
                            AiProviderType.GOOGLE, AiProviderType.OPENAI,
                            AiProviderType.ANTHROPIC, AiProviderType.DEEPSEEK,
                            AiProviderType.ZHIPU, AiProviderType.QWEN,
                            AiProviderType.OPENROUTER, AiProviderType.CUSTOM
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            providers.chunked(4).forEach { rowProviders ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowProviders.forEach { provider ->
                                        ProviderTile(
                                            provider = provider,
                                            selected = activeProvider == provider,
                                            onClick = { onProviderSelected(provider) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.label_api_key),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = draftApiKey,
                            onValueChange = {
                                draftApiKey = it
                                onConfigUpdated(activeProvider, it, draftBaseUrl, draftModel)
                            },
                            singleLine = true,
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            shape = WeatherTheme.shapes.Pill,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ai_label_base_url),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = if (activeProvider == AiProviderType.CUSTOM) {
                                    draftBaseUrl
                                } else {
                                    getOfficialBaseUrl(activeProvider)
                                },
                                onValueChange = {
                                    if (activeProvider == AiProviderType.CUSTOM) {
                                        draftBaseUrl = it
                                        onConfigUpdated(activeProvider, draftApiKey, it, draftModel)
                                    }
                                },
                                enabled = activeProvider == AiProviderType.CUSTOM,
                                singleLine = true,
                                shape = WeatherTheme.shapes.Input,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.ai_base_url_hint),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ai_label_model),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )

                            OutlinedTextField(
                                value = draftModel,
                                onValueChange = {
                                    draftModel = it
                                    onConfigUpdated(activeProvider, draftApiKey, draftBaseUrl, it)
                                },
                                placeholder = {
                                    Text(
                                        text = getDefaultModelName(activeProvider),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                shape = WeatherTheme.shapes.Input,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.ai_model_hint),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(WeatherTheme.shapes.Pill)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, WeatherTheme.shapes.Pill)
                                .bounceClickable(onClick = {
                                    onConfigUpdated(activeProvider, draftApiKey, draftBaseUrl, draftModel)
                                    onTestConnection()
                                })
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            if (aiState.isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.ai_test_connection),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        aiState.testResult?.let { result ->
                            val dotColor = when {
                                !result.isSuccess -> MaterialTheme.colorScheme.error
                                result.latencyMs < 3000 -> Color(0xFF2E7D32)
                                result.latencyMs < 8000 -> Color(0xFFFBC02D)
                                else -> Color(0xFFEF6C00)
                            }
                            val statusText = if (result.isSuccess) {
                                stringResource(R.string.ai_test_latency, result.latencyMs)
                            } else {
                                when (result.errorMessage) {
                                    AiErrorKeys.MISSING_API_KEY -> stringResource(R.string.ai_error_missing_api_key)
                                    AiErrorKeys.CONNECTION_FAILED -> stringResource(R.string.ai_error_connection_failed)
                                    else -> result.errorMessage?.takeIf { it.isNotBlank() }
                                        ?: stringResource(R.string.ai_test_failed)
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Text(
                                    text = statusText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dotColor
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.ai_flame_service_notice),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
private fun ProviderTile(
    provider: AiProviderType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val svgResName = when (provider) {
        AiProviderType.GOOGLE -> "google"
        AiProviderType.OPENAI -> "openai"
        AiProviderType.ANTHROPIC -> "anthropic"
        AiProviderType.DEEPSEEK -> "deepseek"
        AiProviderType.ZHIPU -> "zhipu"
        AiProviderType.QWEN -> "alibabacloud"
        AiProviderType.OPENROUTER -> "openrouter"
        AiProviderType.CUSTOM -> "custom"
    }

    val customDrawableId = remember(svgResName) {
        context.resources.getIdentifier(svgResName, "drawable", context.packageName)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (customDrawableId != 0) {
                Icon(
                    painter = painterResource(id = customDrawableId),
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = when (provider) {
                        AiProviderType.GOOGLE -> Icons.Default.Psychology
                        AiProviderType.OPENAI -> Icons.Default.AutoAwesome
                        AiProviderType.ANTHROPIC -> Icons.Default.Settings
                        AiProviderType.DEEPSEEK -> Icons.Default.Psychology
                        AiProviderType.ZHIPU -> Icons.Default.Tune
                        AiProviderType.QWEN -> Icons.Default.Psychology
                        AiProviderType.OPENROUTER -> Icons.Default.AutoAwesome
                        AiProviderType.CUSTOM -> Icons.Default.Code
                    },
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = stringResource(provider.labelRes()),
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@StringRes
private fun AiProviderType.labelRes(): Int = when (this) {
    AiProviderType.GOOGLE -> R.string.ai_provider_gemini
    AiProviderType.OPENAI -> R.string.ai_provider_openai
    AiProviderType.ANTHROPIC -> R.string.ai_provider_anthropic
    AiProviderType.DEEPSEEK -> R.string.ai_provider_deepseek
    AiProviderType.ZHIPU -> R.string.ai_provider_zhipu
    AiProviderType.QWEN -> R.string.ai_provider_qwen
    AiProviderType.OPENROUTER -> R.string.ai_provider_openrouter
    AiProviderType.CUSTOM -> R.string.ai_provider_custom
}

@Composable
private fun AdviceCard(icon: ImageVector, title: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WeatherTheme.shapes.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), WeatherTheme.shapes.Card)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

private fun getOfficialBaseUrl(provider: AiProviderType): String = AiProviderDefaults.baseUrl(provider)

private fun getDefaultModelName(provider: AiProviderType): String = AiProviderDefaults.model(provider)