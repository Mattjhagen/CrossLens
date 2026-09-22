package com.crosslens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val homepage: String,
    val countryCodes: List<String>,
    val regionIds: List<String>,
    val defaultLanguages: List<String>
)
