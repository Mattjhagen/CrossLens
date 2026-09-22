package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val sourceId: String,
    val originalUrl: String,
    val publishedTime: Instant,
    val originalLanguage: String,
    val originalHeadline: String,
    val originalExcerpt: String,
    val attribution: String,
    val isDemo: Boolean
)
