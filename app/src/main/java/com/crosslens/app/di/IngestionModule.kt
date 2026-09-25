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

    @Provides
    @Singleton
    fun provideSourceHealthMonitor(): SourceHealthMonitor {
        return SourceHealthMonitor()
    }

    @Provides
    @Singleton
    fun provideRssSourceAdapters(
        httpClient: OkHttpClient,
        healthMonitor: SourceHealthMonitor
    ): List<RssSourceAdapter> {
        return RssSourceAdapter.createApprovedSources(httpClient, healthMonitor)
    }
}
