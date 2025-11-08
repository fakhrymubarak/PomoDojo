package com.fakhry.pomodojo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.navigation.AppNavHost
import com.fakhry.pomodojo.preferences.data.source.PreferenceKeys
import com.fakhry.pomodojo.preferences.data.source.provideDataStore
import com.fakhry.pomodojo.preferences.domain.model.AppTheme
import com.fakhry.pomodojo.preferences.domain.model.PreferencesDomain
import com.fakhry.pomodojo.preferences.domain.usecase.PreferencesRepository
import com.fakhry.pomodojo.ui.theme.PomoDojoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val initialThemeState = rememberInitialTheme()
    val initialTheme = initialThemeState.value ?: run {
        PomoDojoTheme {
            // Awaiting stored theme before starting the DI graph.
        }
        return
    }
    val initialPreferences = remember(initialTheme) {
        PreferencesDomain(appTheme = initialTheme)
    }

    KoinApplication(
        application = { modules(composeAppModules) }
    ) {
        val preferencesRepository = koinInject<PreferencesRepository>()
        val preferences by preferencesRepository.preferences.collectAsState(initial = initialPreferences)
        val useDarkTheme = preferences.appTheme == AppTheme.DARK

        PomoDojoTheme(darkTheme = useDarkTheme) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun rememberInitialTheme(): State<AppTheme?> {
    val dataStore = remember { provideDataStore() }
    return produceState(initialValue = null, key1 = dataStore) {
        value = runCatching {
            dataStore.data
                .map { prefs -> AppTheme.fromStorage(prefs[PreferenceKeys.APP_THEME]) }
                .first()
        }.getOrDefault(AppTheme.DARK)
    }
}
