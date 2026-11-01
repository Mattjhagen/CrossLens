package com.crosslens.app.data.sync

import android.content.Context
import androidx.work.*
import com.crosslens.app.data.repository.LiveStoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

/** Opportunistic refresh, not a promise of real-time news or exact scheduling. */
class FeedSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun liveRepository(): LiveStoryRepository
    }

    override suspend fun doWork(): Result {
        val repository = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java).liveRepository()
        return if (repository.refreshIfStale().isSuccess) Result.success()
        else if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<FeedSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("news-cache-refresh", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
