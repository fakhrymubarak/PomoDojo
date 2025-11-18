package com.fakhry.pomodojo.features.focus.domain.model

import com.fakhry.pomodojo.shared.domain.model.quote.QuoteContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuoteContentTest {
    @Test
    fun `withAttribution concatenates available metadata`() {
        val quote =
            QuoteContent(
                text = "Stay focused",
                character = "Sensei",
                sourceTitle = "Dojo Scrolls",
                metadata = "Vol. 2",
            )

        val attribution = quote.attribution()
        assertEquals("Sensei — Dojo Scrolls — Vol. 2", attribution)
        assertEquals("\"Stay focused\" Sensei — Dojo Scrolls — Vol. 2", quote.withAttribution())
    }

    @Test
    fun `attribution handles empty metadata`() {
        val quote =
            QuoteContent(
                text = "Believe",
                character = null,
                sourceTitle = null,
                metadata = "   ",
            )

        assertTrue(quote.attribution().isEmpty())
        assertEquals("\"Believe\" ", quote.withAttribution())
    }
}
