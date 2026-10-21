package app.flameproject.weather.data.ai

import app.flameproject.weather.data.local.AiProviderConfig
import app.flameproject.weather.data.local.AiProviderType
import app.flameproject.weather.data.remote.NetworkClient
import app.flameproject.weather.domain.model.AiAnalysisResult
import app.flameproject.weather.domain.model.AiConnectionTestResult
import app.flameproject.weather.domain.model.AiErrorKeys
import app.flameproject.weather.domain.model.AiStreamEvent
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Handles direct client-side execution against third-party AI endpoints.
 */
class AiRepository {

    private val json = NetworkClient.json
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getDefaultBaseUrl(provider: AiProviderType): String = AiProviderDefaults.baseUrl(provider)

    fun getDefaultModel(provider: AiProviderType): String = AiProviderDefaults.model(provider)

    suspend fun testConnection(
        provider: AiProviderType,
        config: AiProviderConfig
    ): AiConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isEmpty()) {
            return@withContext AiConnectionTestResult(false, 0, AiErrorKeys.MISSING_API_KEY)
        }

        val baseUrl = config.baseUrl.ifBlank { getDefaultBaseUrl(provider) }.trimEnd('/')
        val model = config.model.ifBlank { getDefaultModel(provider) }
        val startTime = System.currentTimeMillis()

        var attempt = 0
        while (attempt < TEST_MAX_ATTEMPTS) {
            attempt++
            try {
                val request = buildProbeRequest(provider, baseUrl, model, apiKey)
                client.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        return@withContext AiConnectionTestResult(true, latency)
                    }
                    return@withContext AiConnectionTestResult(
                        false,
                        latency,
                        "HTTP ${response.code}: ${response.message}"
                    )
                }
            } catch (e: IOException) {
                if (attempt >= TEST_MAX_ATTEMPTS) {
                    val latency = System.currentTimeMillis() - startTime
                    return@withContext AiConnectionTestResult(false, latency, e.localizedMessage ?: AiErrorKeys.CONNECTION_FAILED)
                }
                delay(retryDelayMillis(attempt))
            }
        }
        AiConnectionTestResult(false, 0, AiErrorKeys.CONNECTION_FAILED)
    }

    /**
     * Streams the model answer, emitting text deltas as they arrive and the parsed result at the end.
     */
    fun analyzeWeatherStream(
        provider: AiProviderType,
        config: AiProviderConfig,
        replyLanguage: String,
        place: Place,
        weather: WeatherData,
        unitSymbol: String,
        locale: Locale
    ): Flow<AiStreamEvent> = flow {
        val apiKey = config.apiKey.trim()
        if (apiKey.isEmpty()) {
            emit(AiStreamEvent.Failed(IllegalStateException(AiErrorKeys.MISSING_API_KEY)))
            return@flow
        }

        val baseUrl = config.baseUrl.ifBlank { getDefaultBaseUrl(provider) }.trimEnd('/')
        val model = config.model.ifBlank { getDefaultModel(provider) }
        val userPrompt = AiPromptBuilder.buildUserText(replyLanguage, place, weather, unitSymbol, locale)

        var attempt = 0
        var streaming = true

        while (attempt < MAX_ATTEMPTS) {
            attempt++
            val buffer = StringBuilder()

            try {
                val raw = withContext(Dispatchers.IO) {
                    val request = buildAnalysisRequest(provider, baseUrl, model, apiKey, userPrompt, streaming)
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            val body = runCatching { response.body?.string().orEmpty() }.getOrDefault("")
                            throw HttpStatusException(response.code, body)
                        }
                        readBody(provider, response, streaming) { delta -> buffer.append(delta) }
                    }
                }

                val extracted = extractJsonPayload(raw)
                    ?: throw UnparseableOutputException(raw)

                val parsed = json.decodeFromString<AiAnalysisResult>(extracted)
                    .copy(providerName = provider.name, modelName = model)

                emit(AiStreamEvent.Completed(parsed))
                return@flow
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val statusError = e as? HttpStatusException
                if (statusError != null && !statusError.isRetryable) {
                    if (streaming) {
                        streaming = false
                        attempt = 0
                        emit(AiStreamEvent.Retry(attempt))
                        continue
                    }
                    emit(AiStreamEvent.Failed(IOException("HTTP ${statusError.code}: ${statusError.body.take(300)}")))
                    return@flow
                }

                if (isRetryable(e) && attempt < MAX_ATTEMPTS) {
                    emit(AiStreamEvent.Retry(attempt))
                    delay(retryDelayMillis(attempt))
                    continue
                }

                emit(AiStreamEvent.Failed(toUserFacingError(e)))
                return@flow
            }
        }
    }

    private fun buildProbeRequest(
        provider: AiProviderType,
        baseUrl: String,
        model: String,
        apiKey: String
    ): Request {
        return when (provider) {
            AiProviderType.GOOGLE -> {
                val url = "$baseUrl/models/$model:generateContent?key=$apiKey"
                val body = buildJsonObject {
                    put("contents", buildJsonArray {
                        add(buildJsonObject {
                            put("parts", buildJsonArray { add(buildJsonObject { put("text", "Reply ok") }) })
                        })
                    })
                }.toString()
                Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()
            }
            AiProviderType.ANTHROPIC -> {
                val url = "$baseUrl/v1/messages"
                val body = buildJsonObject {
                    put("model", model)
                    put("max_tokens", 8)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", "Reply ok")
                        })
                    })
                }.toString()
                Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
            }
            else -> {
                val url = "$baseUrl/chat/completions"
                val body = buildJsonObject {
                    put("model", model)
                    put("max_tokens", 8)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", "Reply ok")
                        })
                    })
                }.toString()
                Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
            }
        }
    }

    private fun buildAnalysisRequest(
        provider: AiProviderType,
        baseUrl: String,
        model: String,
        apiKey: String,
        userPrompt: String,
        streaming: Boolean
    ): Request {
        return when (provider) {
            AiProviderType.GOOGLE -> {
                val action = if (streaming) "streamGenerateContent?alt=sse&" else "generateContent?"
                val url = "$baseUrl/models/$model:$action" + "key=$apiKey"
                val body = buildJsonObject {
                    put("systemInstruction", buildJsonObject {
                        put("parts", buildJsonArray { add(buildJsonObject { put("text", AiPromptBuilder.SYSTEM_PROMPT) }) })
                    })
                    put("contents", buildJsonArray {
                        add(buildJsonObject {
                            put("parts", buildJsonArray { add(buildJsonObject { put("text", userPrompt) }) })
                        })
                    })
                    put("generationConfig", buildJsonObject {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1200)
                        put("responseMimeType", "application/json")
                    })
                }.toString()
                Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()
            }
            AiProviderType.ANTHROPIC -> {
                val url = "$baseUrl/v1/messages"
                val body = buildJsonObject {
                    put("model", model)
                    put("max_tokens", 1200)
                    put("system", AiPromptBuilder.SYSTEM_PROMPT)
                    put("stream", streaming)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", userPrompt)
                        })
                    })
                }.toString()
                Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
            }
            else -> {
                val url = "$baseUrl/chat/completions"
                val body = buildJsonObject {
                    put("model", model)
                    put("temperature", 0.7)
                    put("max_tokens", 1200)
                    put("stream", streaming)
                    put("response_format", buildJsonObject { put("type", "json_object") })
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "system")
                            put("content", AiPromptBuilder.SYSTEM_PROMPT)
                        })
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", userPrompt)
                        })
                    })
                }.toString()
                Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
            }
        }
    }

    private suspend fun readBody(
        provider: AiProviderType,
        response: Response,
        streaming: Boolean,
        onDelta: (String) -> Unit
    ): String {
        val body = response.body ?: throw IOException("Empty response body")
        if (!streaming) {
            return extractEnvelopeText(provider, body.string())
        }

        val source = body.source()
        val buffer = StringBuilder()
        while (!source.exhausted()) {
            currentCoroutineContext().ensureActive()
            val line = source.readUtf8Line() ?: break
            if (!line.startsWith("data:")) continue
            val payload = line.removePrefix("data:").trim()
            if (payload.isEmpty()) continue
            if (payload == "[DONE]") break
            val delta = extractDelta(provider, payload) ?: continue
            if (delta.isNotEmpty()) {
                buffer.append(delta)
                onDelta(delta)
            }
        }
        return buffer.toString()
    }

    private fun extractDelta(provider: AiProviderType, payload: String): String? {
        val root = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return null
        return when (provider) {
            AiProviderType.GOOGLE -> {
                val parts = root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject
                    ?.get("content")?.jsonObject?.get("parts")?.jsonArray ?: return null
                parts.mapNotNull { part ->
                    part.jsonObject["text"]?.jsonPrimitive?.content
                }.joinToString("")
            }
            AiProviderType.ANTHROPIC -> root["delta"]?.jsonObject?.get("text")?.jsonPrimitive?.content
            else -> root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("delta")?.jsonObject?.get("content")?.jsonPrimitive?.content
        }
    }

    private fun extractEnvelopeText(provider: AiProviderType, body: String): String {
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: throw UnparseableOutputException(body)
        return when (provider) {
            AiProviderType.GOOGLE -> {
                val parts = root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject
                    ?.get("content")?.jsonObject?.get("parts")?.jsonArray
                    ?: JsonArray(emptyList())
                parts.mapNotNull { part -> part.jsonObject["text"]?.jsonPrimitive?.content }.joinToString("")
            }
            AiProviderType.ANTHROPIC -> root["content"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content.orEmpty()
            else -> root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content.orEmpty()
        }
    }

    private fun extractJsonPayload(raw: String): String? {
        val trimmed = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex == -1 || endIndex <= startIndex) return null
        return trimmed.substring(startIndex, endIndex + 1)
    }

    private fun isRetryable(error: Throwable): Boolean = when (error) {
        is HttpStatusException -> error.isRetryable
        is UnparseableOutputException -> true
        is IOException -> true
        else -> false
    }

    private fun toUserFacingError(error: Throwable): Throwable = when (error) {
        is HttpStatusException -> IOException("HTTP ${error.code}: ${error.body.take(300)}")
        is UnparseableOutputException -> IOException(AiErrorKeys.UNPARSEABLE)
        else -> error
    }

    private fun retryDelayMillis(attempt: Int): Long = when (attempt) {
        1 -> 800L
        2 -> 2000L
        else -> 4000L
    }

    private class HttpStatusException(val code: Int, val body: String) : IOException("HTTP $code") {
        val isRetryable: Boolean get() = code == 429 || code >= 500
    }

    private class UnparseableOutputException(val raw: String) : IOException("Unparseable model output")

    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val TEST_MAX_ATTEMPTS = 2
    }
}
