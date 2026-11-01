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
    private val storyRepository: MockStoryRepository,
    private val liveRepository: com.crosslens.app.data.repository.LiveStoryRepository
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        appScope.launch {
            try {
                storyRepository.seedData()
                liveRepository.refreshIfStale()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                // A failed refresh must not terminate the process or erase the local cache.
                android.util.Log.e("CrossLensInit", "Local initialization unavailable", e)
            }
        }
    }
}
