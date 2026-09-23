package com.crosslens.app.di

import com.crosslens.app.data.ingestion.EventClusteringPipeline
import com.crosslens.app.data.ingestion.InMemorySourceRegistry
import com.crosslens.app.data.ingestion.SourceRegistry
import com.crosslens.app.data.ingestion.SourceRegistryValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
}
