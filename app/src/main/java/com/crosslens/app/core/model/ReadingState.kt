package com.crosslens.app.core.model

data class ReadingState(
    val savedStoryIds: Set<String>,
    val lastOpenedStoryId: String?
)
