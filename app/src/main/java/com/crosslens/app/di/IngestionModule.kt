package com.crosslens.app.di

import com.crosslens.app.data.ingestion.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IngestionModule {

    @Provides
    @Singleton
    fun provideEventClusteringPipeline(): EventClusteringPipeline {
        return EventClusteringPipeline()
    }

    @Provides
    @Singleton
    fun provideSourceRegistry(): SourceRegistry {
        return InMemorySourceRegistry.createMockRegistry()
    }

    @Provides
    @Singleton
    fun provideSourceRegistryValidator(registry: SourceRegistry): SourceRegistryValidator {
        return SourceRegistryValidator(
            registry = registry,
            allowDemoSources = true // Prototype mode
        )
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        return RssSourceAdapter.createHttpClient()
    }

    // SourceHealthMonitor is now auto-provided by Hilt via @Inject constructor (with DAO dependency)

    @Provides
    @Singleton
    fun provideRssSourceAdapters(
        httpClient: OkHttpClient,
        healthMonitor: SourceHealthMonitor
    ): List<RssSourceAdapter> {
        return RssSourceAdapter.createApprovedSources(httpClient, healthMonitor)
    }

    @Provides
    @Singleton
    fun provideSourceHealthDiagnostic(
        healthMonitor: SourceHealthMonitor,
        rssAdapters: List<RssSourceAdapter>
    ): SourceHealthDiagnostic {
        return SourceHealthDiagnostic(healthMonitor, rssAdapters)
    }
}
