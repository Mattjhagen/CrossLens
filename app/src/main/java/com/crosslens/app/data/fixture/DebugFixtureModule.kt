package com.crosslens.app.data.fixture

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for debug fixture provider.
 *
 * Binds the build-variant-specific implementation:
 * - Debug builds: DebugEventFixtureProviderImpl (returns test fixtures)
 * - Release builds: DebugEventFixtureProviderImpl (returns empty list)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DebugFixtureModule {

    @Binds
    @Singleton
    abstract fun bindDebugEventFixtureProvider(
        impl: DebugEventFixtureProviderImpl
    ): DebugEventFixtureProvider
}
