package com.fakhry.pomodojo.focus.domain.repository

import com.fakhry.pomodojo.focus.domain.model.QuoteContent

/**
 * Provides motivational quotes for focus sessions.
 */
interface QuoteRepository {
    suspend fun randomQuote(): QuoteContent
    suspend fun getById(id: String): QuoteContent
}