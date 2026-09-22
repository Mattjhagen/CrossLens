package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frame_observations")
data class FrameObservationEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val articleIds: List<String>,
    val emphasizedActors: List<String>,
    val emphasizedClaims: List<String>,
    val languageObservation: String?,
    val sentimentObservation: String?,
    val evidenceReferences: List<String>,
    val methodVersion: String
)
