package com.crosslens.app.di

import android.content.Context
import androidx.room.Room
import com.crosslens.app.data.local.CrossLensDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CrossLensDatabase {
        return Room.databaseBuilder(
            context,
            CrossLensDatabase::class.java,
            "crosslens_database"
        )
            .fallbackToDestructiveMigration() // Prototype mode: recreate on schema change
            .build()
    }

    @Provides
    fun provideStoryDao(database: CrossLensDatabase) = database.storyDao()

    @Provides
    fun provideSourceDao(database: CrossLensDatabase) = database.sourceDao()

    @Provides
    fun provideArticleDao(database: CrossLensDatabase) = database.articleDao()

    @Provides
    fun provideTranslationDao(database: CrossLensDatabase) = database.translationDao()

    @Provides
    fun provideClaimDao(database: CrossLensDatabase) = database.claimDao()

    @Provides
    fun provideFrameObservationDao(database: CrossLensDatabase) = database.frameObservationDao()

    @Provides
    fun provideEditorialDecisionDao(database: CrossLensDatabase) = database.editorialDecisionDao()

    @Provides
    fun provideEditorialReviewDao(database: CrossLensDatabase) = database.editorialReviewDao()

    @Provides
    fun provideFeedMetadataDao(database: CrossLensDatabase) = database.feedMetadataDao()
}
