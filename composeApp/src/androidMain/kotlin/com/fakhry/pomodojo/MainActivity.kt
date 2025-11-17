package com.fakhry.pomodojo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fakhry.pomodojo.preferences.domain.model.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
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
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
