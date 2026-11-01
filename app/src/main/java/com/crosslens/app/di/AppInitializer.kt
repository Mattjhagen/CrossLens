package com.crosslens.app.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val liveRepository: com.crosslens.app.data.repository.LiveStoryRepository
) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        appScope.launch {
            try {
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
