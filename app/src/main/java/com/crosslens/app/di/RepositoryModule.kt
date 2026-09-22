package com.crosslens.app.di

import com.crosslens.app.data.mock.MockStoryRepository
import com.crosslens.app.data.preferences.*
import com.crosslens.app.data.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStoryRepository(impl: MockStoryRepository): StoryRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DataStoreUserPreferencesRepository
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindReadingStateRepository(
        impl: DataStoreReadingStateRepository
    ): ReadingStateRepository

    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(
        impl: DataStoreEntitlementRepository
    ): EntitlementRepository
}
