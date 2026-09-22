package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val id: String,
    val articleId: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatedHeadline: String?,
    val translatedExcerpt: String?,
    val status: String, // TranslationStatus enum name
    val method: String?,
    val provider: String?,
    val generatedTime: Instant?
)
