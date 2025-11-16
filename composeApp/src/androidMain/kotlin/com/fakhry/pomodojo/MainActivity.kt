package com.fakhry.pomodojo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fakhry.pomodojo.preferences.data.source.AndroidPreferencesDataStoreProvider
import com.fakhry.pomodojo.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        val preferences = getAndroidPreferences()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setSplashScreenTheme(resolveAppTheme(preferences.appTheme))
        }
        super.onCreate(savedInstanceState)

        setContent {
            App(
                appTheme = preferences.appTheme,
                hasActiveSession = preferences.hasActiveSession,
                onThemeUpdated = { appTheme ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        splashScreen.setSplashScreenTheme(resolveAppTheme(appTheme))
                    }
                },
            )
        }
    }

    private fun resolveAppTheme(appTheme: AppTheme): Int = when (appTheme) {
        AppTheme.DARK -> R.style.Theme_PomoDojo_Splash_Dark
        AppTheme.LIGHT -> R.style.Theme_PomoDojo_Splash_Light
    }

    private fun getAndroidPreferences(): PreferencesDomain {
        val preferences = runBlocking {
            runCatching {
                AndroidPreferencesDataStoreProvider.dataStore.data.map { prefs ->
                    // Take only initial data for more optimal processing
                    PreferencesDomain(
                        appTheme = AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]),
                        hasActiveSession = prefs[PreferenceKeys.HAS_ACTIVE_SESSION] ?: false,
                    )
                }.first()
            }.getOrDefault(PreferencesDomain())
        }

        return preferences
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
