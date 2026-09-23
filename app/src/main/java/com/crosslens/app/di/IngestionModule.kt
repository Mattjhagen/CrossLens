package com.crosslens.app.di

import com.crosslens.app.data.ingestion.EventClusteringPipeline
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
}
