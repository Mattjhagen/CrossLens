package com.crosslens.app.di

import com.crosslens.app.data.mock.MockStoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val storyRepository: MockStoryRepository
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        appScope.launch {
            storyRepository.seedData()
        }
    }
}
