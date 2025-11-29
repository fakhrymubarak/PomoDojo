package com.fakhry.pomodojo.shared.domain.model.quote

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
    fun withAttribution() = "\"$text\" ${attribution()}"
    fun attribution() = listOfNotNull(
        character,
        sourceTitle,
        metadata,
    ).joinToString(separator = " — ").takeIf { it.isNotBlank() }.orEmpty()

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
