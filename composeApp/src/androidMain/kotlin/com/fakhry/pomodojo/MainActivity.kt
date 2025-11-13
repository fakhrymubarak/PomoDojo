package com.fakhry.pomodojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fakhry.pomodojo.focus.data.db.initAndroidAppContextHolder
import com.fakhry.pomodojo.focus.data.db.initAndroidFocusDatabase
import com.fakhry.pomodojo.preferences.data.source.initAndroidPreferenceStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initAndroidAppContextHolder(applicationContext)
        initAndroidPreferenceStorage(applicationContext)
        initAndroidFocusDatabase(applicationContext)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
