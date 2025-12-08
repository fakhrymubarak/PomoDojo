package com.fakhry.pomodojo.features.focus.domain.repository

import com.fakhry.pomodojo.domain.pomodoro.model.quote.QuoteContent

/**
 * Provides motivational quotes for focus sessions.
 */
interface QuoteRepository {
    suspend fun randomQuote(): QuoteContent

    suspend fun getById(id: String): QuoteContent
}
