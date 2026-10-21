package app.flameproject.weather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.flameproject.weather.data.ai.AiRepository
import app.flameproject.weather.data.local.DarkModePreference
import app.flameproject.weather.data.local.UserPreferencesRepository
import app.flameproject.weather.data.repository.WeatherRepository
import app.flameproject.weather.domain.location.LocationTracker
import app.flameproject.weather.ui.locale.localized
import app.flameproject.weather.ui.screens.MainScreen
import app.flameproject.weather.ui.theme.WeatherTheme
import app.flameproject.weather.ui.viewmodel.AiViewModel
import app.flameproject.weather.ui.viewmodel.SettingsViewModel
import app.flameproject.weather.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val weatherRepository = WeatherRepository(preferencesRepository)
        val aiRepository = AiRepository()
        val locationTracker = LocationTracker(applicationContext)

        val weatherViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WeatherViewModel(weatherRepository, preferencesRepository, locationTracker) as T
            }
        })[WeatherViewModel::class.java]

        val aiViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AiViewModel(aiRepository, preferencesRepository) as T
            }
        })[AiViewModel::class.java]

        val settingsViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(preferencesRepository) as T
            }
        })[SettingsViewModel::class.java]

        setContent {
            val preferences by settingsViewModel.preferences.collectAsState()
            val context = LocalContext.current

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (granted) {
                    weatherViewModel.loadInitialLocation()
                }
            }

            LaunchedEffect(Unit) {
                if (!locationTracker.hasLocationPermission()) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            val localizedContext = context.localized(preferences.language)

            val isDark = when (preferences.darkMode) {
                DarkModePreference.SYSTEM -> isSystemInDarkTheme()
                DarkModePreference.LIGHT -> false
                DarkModePreference.DARK -> true
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalContext provides localizedContext
            ) {
                WeatherTheme(
                    seed = preferences.paletteSeed,
                    customHex = preferences.customColorHex,
                    colorStyle = preferences.paletteStyle,
                    pureBlack = preferences.pureBlack,
                    darkTheme = isDark
                ) {
                    MainScreen(
                        weatherViewModel = weatherViewModel,
                        aiViewModel = aiViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}