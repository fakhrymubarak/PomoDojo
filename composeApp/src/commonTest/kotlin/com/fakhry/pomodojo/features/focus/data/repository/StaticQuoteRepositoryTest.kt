package com.fakhry.pomodojo.features.focus.data.repository

import com.fakhry.pomodojo.core.utils.primitives.isNotEmptyAndNonNull
import com.fakhry.pomodojo.shared.domain.model.quote.QuoteContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StaticQuoteRepositoryTest {
    private val repository = StaticQuoteRepository()

    @Test
    fun `randomQuote returns a valid quote from collection`() = runTest {
        val quote = repository.randomQuote()

        assertNotNull(quote)
        assertNotNull(quote.id)
        assertNotNull(quote.text)
        assertNotNull(quote.character)
        assertNotNull(quote.sourceTitle)
        assertTrue(quote.id.isNotEmpty())
        assertTrue(quote.text.isNotEmpty())
        assertTrue(quote.character.isNotEmptyAndNonNull())
        assertTrue(quote.sourceTitle.isNotEmptyAndNonNull())
    }

    @Test
    fun `randomQuote returns different quotes on multiple calls`() = runTest {
        val quotes = List(20) { repository.randomQuote() }
        val uniqueIds = quotes.map { it.id }.toSet()

        // With 100 quotes, getting at least 2 different quotes in 20 calls is highly probable
        assertTrue(uniqueIds.size > 1, "Expected multiple unique quotes, but got ${uniqueIds.size}")
    }

    @Test
    fun `getById returns the correct quote`() = runTest {
        val expectedId = "quote-haikyuu-01"
        val quote = repository.getById(expectedId)

        assertEquals(expectedId, quote.id)
        assertEquals(
            "Talent is something you make bloom. Instinct is something you polish!",
            quote.text,
        )
        assertEquals("Tooru Oikawa", quote.character)
        assertEquals("Haikyuu!!", quote.sourceTitle)
        assertEquals("Season 2, Episode 24", quote.metadata)
    }

    @Test
    fun `getById with another known id returns correct quote`() = runTest {
        val expectedId = "quote-naruto-01"
        val quote = repository.getById(expectedId)

        assertEquals(expectedId, quote.id)
        assertEquals(
            "Hard work is worthless for those that don't believe in themselves.",
            quote.text,
        )
        assertEquals("Naruto Uzumaki", quote.character)
        assertEquals("Naruto", quote.sourceTitle)
        assertEquals(null, quote.metadata)
    }

    @Test
    fun `getById with non-existent id returns default quote`() = runTest {
        val quote = repository.getById("non-existent-id")

        assertEquals(QuoteContent.DEFAULT_QUOTE, quote)
    }

    @Test
    fun `getById with empty id returns default quote`() = runTest {
        val quote = repository.getById("")

        assertEquals(QuoteContent.DEFAULT_QUOTE, quote)
    }

    @Test
    fun `repository contains multiple anime series`() = runTest {
        val quotes = List(50) { repository.randomQuote() }
        val sources = quotes.map { it.sourceTitle }.toSet()

        // Verify we have quotes from multiple different anime series
        assertTrue(sources.size > 5, "Expected quotes from multiple series, got ${sources.size}")
        assertTrue(
            sources.contains("Naruto") ||
                sources.contains("One Piece") ||
                sources.contains("Haikyuu!!"),
        )
    }

    @Test
    fun `all quotes have required fields`() = runTest {
        // Get a good sample of quotes by calling randomQuote multiple times
        val quotes = List(50) { repository.randomQuote() }.distinctBy { it.id }

        quotes.forEach { quote ->
            assertTrue(quote.id.isNotEmpty(), "Quote has empty id")
            assertTrue(quote.text.isNotEmpty(), "Quote ${quote.id} has empty text")
            assertTrue(
                quote.character?.isNotEmpty() == true,
                "Quote ${quote.id} has empty character",
            )
            assertTrue(
                quote.sourceTitle?.isNotEmpty() == true,
                "Quote ${quote.id} has empty sourceTitle",
            )
            // metadata can be null, so we don't check it
        }
    }

    @Test
    fun `getById is case sensitive`() = runTest {
        val quote1 = repository.getById("quote-haikyuu-01")
        val quote2 = repository.getById("QUOTE-HAIKYUU-01")

        assertNotEquals(QuoteContent.DEFAULT_QUOTE, quote1)
        assertEquals(QuoteContent.DEFAULT_QUOTE, quote2)
    }

    @Test
    fun `randomQuote returns quotes from the collection`() = runTest {
        val quotes = List(30) { repository.randomQuote() }

        quotes.forEach { quote ->
            assertNotNull(quote.id)
            assertNotNull(quote.text)
            assertNotNull(quote.character)
            assertNotNull(quote.sourceTitle)
        }
    }
}
