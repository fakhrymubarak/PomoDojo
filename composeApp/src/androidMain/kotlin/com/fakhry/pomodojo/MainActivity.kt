package com.fakhry.pomodojo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Exposes test tags as resource ids for Maestro (see .maestro/).
            Box(Modifier.semantics { testTagsAsResourceId = true }) {
                App(
                    onThemeUpdated = { appTheme ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            splashScreen.setSplashScreenTheme(resolveAppTheme(appTheme))
                        }
                    },
                )
            }
        }
    }

    private fun resolveAppTheme(appTheme: String): Int = when (appTheme) {
        "dark" -> R.style.Theme_PomoDojo_Splash_Dark
        else -> R.style.Theme_PomoDojo_Splash_Light
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
