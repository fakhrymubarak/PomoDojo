package com.fakhry.pomodojo.preferences

import com.fakhry.pomodojo.di.composeAppModules
import com.fakhry.pomodojo.preferences.ui.PreferencesViewModel
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertNotNull

class PreferencesModuleTest {

    @Test
    fun `can resolve preferences view model`() {
        val app = startKoin {
            modules(composeAppModules)
        }
        try {
            val viewModel = app.koin.get<PreferencesViewModel>()
            assertNotNull(viewModel)
        } finally {
            stopKoin()
        }
    }
}
