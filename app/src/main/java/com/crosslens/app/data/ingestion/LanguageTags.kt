package com.crosslens.app.data.ingestion

import java.util.IllformedLocaleException
import java.util.Locale

/** Declared metadata normalization only. This does not infer language from article text. */
object LanguageTags {
    fun normalize(tag: String?): String? {
        if (tag.isNullOrBlank()) return null
        return try {
            val locale = Locale.Builder().setLanguageTag(tag.trim()).build()
            locale.toLanguageTag().takeIf { it == "und" || locale.language.length in 2..3 }
        } catch (_: IllformedLocaleException) { null }
    }
}
