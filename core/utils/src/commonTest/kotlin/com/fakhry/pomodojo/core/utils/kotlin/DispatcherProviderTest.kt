package com.fakhry.pomodojo.core.utils.kotlin

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals

class DispatcherProviderTest {
    @Test
    fun `test constructor uses provided dispatcher for all contexts`() {
        val dispatcher = StandardTestDispatcher()
        val provider = DispatcherProvider(dispatcher)

        assertEquals(dispatcher, provider.main)
        assertEquals(dispatcher, provider.io)
        assertEquals(dispatcher, provider.computation)
    }
}
