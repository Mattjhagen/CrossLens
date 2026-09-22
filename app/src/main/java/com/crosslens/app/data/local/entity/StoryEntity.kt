package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val eventTime: Instant?,
    val updatedTime: Instant,
    val topicIds: List<String>,
    val eventCountryCodes: List<String>,
    val articleIds: List<String>,
    val claimIds: List<String>,
    val lensGapScore: Int?,
    val lensGapStatus: String,
    val lensGapIsDemo: Boolean
)
