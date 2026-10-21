package app.flameproject.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.flameproject.weather.data.ai.AiRepository
import app.flameproject.weather.data.local.AiProviderConfig
import app.flameproject.weather.data.local.AiProviderType
import app.flameproject.weather.data.local.TemperatureUnit
import app.flameproject.weather.data.local.UserPreferencesRepository
import app.flameproject.weather.ui.locale.toLocale
import app.flameproject.weather.domain.model.AiAnalysisResult
import app.flameproject.weather.domain.model.AiConnectionTestResult
import app.flameproject.weather.domain.model.AiErrorKeys
import app.flameproject.weather.domain.model.AiPartialParser
import app.flameproject.weather.domain.model.AiStreamEvent
import app.flameproject.weather.domain.model.Place
import app.flameproject.weather.domain.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AiUiState(
    val activeProvider: AiProviderType = AiProviderType.GOOGLE,
    val config: AiProviderConfig = AiProviderConfig(),
    val isRunning: Boolean = false,
    val analysisResult: AiAnalysisResult? = null,
    val errorMessage: String? = null,
    val isTestingConnection: Boolean = false,
    val testResult: AiConnectionTestResult? = null,
    val streamingAdvice: Map<String, String> = emptyMap(),
    val retryAttempt: Int = 0
)

/**
 * Handles AI prompt execution, model isolation, and connectivity verification.
 */
class AiViewModel(
    private val aiRepository: AiRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                val currentConfig = prefs.aiProviders[prefs.activeAiProvider] ?: AiProviderConfig()
                _uiState.value = _uiState.value.copy(
                    activeProvider = prefs.activeAiProvider,
                    config = currentConfig
                )
            }
        }
    }

    fun selectProvider(provider: AiProviderType) {
        viewModelScope.launch {
            preferencesRepository.setActiveAiProvider(provider)
        }
    }

    fun updateConfig(targetProvider: AiProviderType, apiKey: String, baseUrl: String, model: String) {
        viewModelScope.launch {
            preferencesRepository.updateAiProviderConfig(
                targetProvider,
                AiProviderConfig(apiKey, baseUrl, model)
            )
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true, testResult = null)
            val result = aiRepository.testConnection(_uiState.value.activeProvider, _uiState.value.config)
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                testResult = result
            )
        }
    }

    fun runAnalysis(place: Place?, weatherData: WeatherData?, language: String) {
        if (place == null || weatherData == null) return

        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val unit = prefs.temperatureUnit
            val unitSymbol = if (unit == TemperatureUnit.FAHRENHEIT) "°F" else "°C"

            _uiState.value = _uiState.value.copy(
                isRunning = true,
                errorMessage = null,
                analysisResult = null,
                streamingAdvice = emptyMap(),
                retryAttempt = 0
            )

            val buffer = StringBuilder()
            aiRepository.analyzeWeatherStream(
                provider = _uiState.value.activeProvider,
                config = _uiState.value.config,
                replyLanguage = language,
                place = place,
                weather = weatherData,
                unitSymbol = unitSymbol,
                locale = prefs.language.toLocale()
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Delta -> {
                        buffer.append(event.text)
                        _uiState.value = _uiState.value.copy(
                            streamingAdvice = AiPartialParser.extract(buffer.toString())
                        )
                    }
                    is AiStreamEvent.Retry -> {
                        buffer.clear()
                        _uiState.value = _uiState.value.copy(
                            streamingAdvice = emptyMap(),
                            retryAttempt = event.attempt
                        )
                    }
                    is AiStreamEvent.Completed -> {
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            analysisResult = event.result,
                            streamingAdvice = emptyMap(),
                            retryAttempt = 0,
                            errorMessage = null
                        )
                    }
                    is AiStreamEvent.Failed -> {
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            streamingAdvice = emptyMap(),
                            retryAttempt = 0,
                            errorMessage = event.error.localizedMessage ?: AiErrorKeys.ANALYSIS_FAILED
                        )
                    }
                }
            }
        }
    }
}