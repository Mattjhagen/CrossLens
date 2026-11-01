package com.crosslens.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    @Test fun `persisted identifiers round trip punctuation unicode and empty lists`() {
        val converter = Converters()
        val values = listOf("https://publisher.example/a?items=one,two", "quoted \"headline\"", "مرحبا", "日本語")
        assertEquals(values, converter.toStringList(converter.fromStringList(values)))
        assertEquals(emptyList<String>(), converter.toStringList(converter.fromStringList(emptyList())))
        assertEquals(values.toSet(), converter.toStringSet(converter.fromStringSet(values.toSet())))
    }
}
