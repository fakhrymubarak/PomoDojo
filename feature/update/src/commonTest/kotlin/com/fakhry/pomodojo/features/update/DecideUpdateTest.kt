package com.fakhry.pomodojo.features.update

import kotlin.test.Test
import kotlin.test.assertEquals

class DecideUpdateTest {

    @Test
    fun `defaults ask for nothing`() {
        assertEquals(UpdateType.NONE, decideUpdate(current = 2, min = 0, latest = 0, dismissed = 0))
    }

    @Test
    fun `below min forces an update even when declined`() {
        assertEquals(
            UpdateType.FORCE,
            decideUpdate(current = 2, min = 3, latest = 3, dismissed = 3),
        )
    }

    @Test
    fun `below latest offers a soft update`() {
        assertEquals(UpdateType.SOFT, decideUpdate(current = 2, min = 2, latest = 3, dismissed = 0))
    }

    @Test
    fun `declined latest stays quiet until a newer one ships`() {
        assertEquals(UpdateType.NONE, decideUpdate(current = 2, min = 0, latest = 3, dismissed = 3))
        assertEquals(UpdateType.SOFT, decideUpdate(current = 2, min = 0, latest = 4, dismissed = 3))
    }

    @Test
    fun `up to date asks for nothing`() {
        assertEquals(UpdateType.NONE, decideUpdate(current = 3, min = 3, latest = 3, dismissed = 0))
    }
}
