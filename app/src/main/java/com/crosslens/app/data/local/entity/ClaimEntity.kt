package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val statement: String,
    val assessment: String, // ClaimAssessment enum name
    val supportingArticleIds: List<String>,
    val contradictingArticleIds: List<String>,
    val assessmentProvenance: String
)
