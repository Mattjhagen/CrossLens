package com.crosslens.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crosslens.app.data.local.dao.*
import com.crosslens.app.data.local.entity.*

@Database(
    entities = [
        StoryEntity::class,
        SourceEntity::class,
        ArticleEntity::class,
        TranslationEntity::class,
        ClaimEntity::class,
        FrameObservationEntity::class,
        EditorialDecisionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CrossLensDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun sourceDao(): SourceDao
    abstract fun articleDao(): ArticleDao
    abstract fun translationDao(): TranslationDao
    abstract fun claimDao(): ClaimDao
    abstract fun frameObservationDao(): FrameObservationDao
    abstract fun editorialDecisionDao(): EditorialDecisionDao
}
