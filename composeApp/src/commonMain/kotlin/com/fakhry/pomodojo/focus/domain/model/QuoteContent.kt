package com.fakhry.pomodojo.focus.domain.model

/**
 * Quote displayed during a focus session.
 */
data class QuoteContent(
    val id: String = "",
    val text: String = "",
    val character: String? = null,
    val sourceTitle: String? = null,
    val metadata: String? = null,
) {
    companion object {
        val DEFAULT_QUOTE =
            QuoteContent(
                id = "quote-naruto-01",
                text = "Hard work is worthless for those that don't believe in themselves.",
                character = "Naruto Uzumaki",
                sourceTitle = "Naruto",
                metadata = null,
            )
    }
}
