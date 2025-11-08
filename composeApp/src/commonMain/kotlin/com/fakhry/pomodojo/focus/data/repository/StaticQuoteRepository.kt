package com.fakhry.pomodojo.focus.data.repository

import com.fakhry.pomodojo.focus.domain.model.QuoteContent
import com.fakhry.pomodojo.focus.domain.repository.QuoteRepository
import kotlin.random.Random

class StaticQuoteRepository : QuoteRepository {

    private val quotes: List<QuoteContent> = listOf(
        QuoteContent(
            id = "quote-haikyuu-01",
            text = "Talent is something you make bloom. Instinct is something you polish!",
            character = "Tooru Oikawa",
            sourceTitle = "Haikyuu!!",
            metadata = "Season 2, Episode 24",
        ),
        QuoteContent(
            id = "quote-demonslayer-01",
            text = "No matter how many people you may lose, you have no choice but to go on living.",
            character = "Tanjiro Kamado",
            sourceTitle = "Demon Slayer",
            metadata = null,
        ),
        QuoteContent(
            id = "quote-onepiece-01",
            text = "If you don't take risks, you can't create a future.",
            character = "Monkey D. Luffy",
            sourceTitle = "One Piece",
            metadata = null,
        ),
        QuoteContent(
            id = "quote-naruto-01",
            text = "Hard work is worthless for those that don't believe in themselves.",
            character = "Naruto Uzumaki",
            sourceTitle = "Naruto",
            metadata = null,
        ),
    )

    override suspend fun randomQuote(): QuoteContent =
        quotes.takeIf { it.isNotEmpty() }?.let { list -> list[Random.Default.nextInt(list.size)] } ?: QuoteContent.DEFAULT_QUOTE

    override suspend fun getById(id: String): QuoteContent =
        quotes.firstOrNull { it.id == id } ?: QuoteContent.DEFAULT_QUOTE
}