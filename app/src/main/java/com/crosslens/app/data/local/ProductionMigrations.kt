package com.crosslens.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Non-destructive upgrades reconstructed from committed entity history. See schema-history README.
 * Version 7 has no committed schema evidence and is deliberately unsupported, not guessed.
 * Runtime Room validation and actual APK-to-APK upgrade tests are mandatory release gates.
 */
object ProductionMigrations {
    val supportedStartVersions = intArrayOf(1, 2, 3, 4, 5, 6, 8, 9, 10)
    val ALL: Array<Migration> = supportedStartVersions.map { start ->
        object : Migration(start, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (start < 4) {
                    db.execSQL(ARTICLE_COPY)
                    db.execSQL("""INSERT INTO `_production_articles` SELECT `id`, `storyId`, `sourceId`,
                        `originalUrl`, `publishedTime`, `originalLanguage`, `originalHeadline`, `originalExcerpt`,
                        `originalExcerpt`, `attribution`, `isDemo`, 0, NULL FROM `articles`""")
                    db.execSQL("DROP TABLE `articles`")
                    db.execSQL("ALTER TABLE `_production_articles` RENAME TO `articles`")
                } else if (start < 8) {
                    db.execSQL("ALTER TABLE `articles` ADD COLUMN `imageUrl` TEXT")
                }
                if (start < 5) {
                    db.execSQL(SOURCE_COPY)
                    db.execSQL("""INSERT INTO `_production_sources` SELECT `id`, `name`, `homepage`,
                        `countryCodes`, `regionIds`, `defaultLanguages`, 0, NULL, NULL FROM `sources`""")
                    db.execSQL("DROP TABLE `sources`")
                    db.execSQL("ALTER TABLE `_production_sources` RENAME TO `sources`")
                }
                if (start < 8) db.execSQL("ALTER TABLE `stories` ADD COLUMN `imageUrl` TEXT")
                NEW_TABLES.forEach(db::execSQL)
            }
        }
    }.toTypedArray()

    private const val ARTICLE_COPY = """CREATE TABLE IF NOT EXISTS `_production_articles` (`id` TEXT NOT NULL, `storyId` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `originalUrl` TEXT NOT NULL, `publishedTime` INTEGER NOT NULL, `originalLanguage` TEXT NOT NULL, `originalHeadline` TEXT NOT NULL, `originalExcerpt` TEXT NOT NULL, `originalContent` TEXT NOT NULL, `attribution` TEXT NOT NULL, `isDemo` INTEGER NOT NULL, `requiresSubscription` INTEGER NOT NULL, `imageUrl` TEXT, PRIMARY KEY(`id`))"""
    private const val SOURCE_COPY = """CREATE TABLE IF NOT EXISTS `_production_sources` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `homepage` TEXT NOT NULL, `countryCodes` TEXT NOT NULL, `regionIds` TEXT NOT NULL, `defaultLanguages` TEXT NOT NULL, `isLocal` INTEGER NOT NULL, `localLocationId` TEXT, `publisherType` TEXT, PRIMARY KEY(`id`))"""
    private val NEW_TABLES = listOf(
        """CREATE TABLE IF NOT EXISTS `editorial_decisions` (`id` TEXT NOT NULL, `proposalId` TEXT NOT NULL, `clusterId` TEXT NOT NULL, `decision` TEXT NOT NULL, `reason` TEXT NOT NULL, `reviewerId` TEXT NOT NULL, `decidedAt` INTEGER NOT NULL, `articleIds` TEXT NOT NULL, `sourceIds` TEXT NOT NULL, `provisionalTitle` TEXT NOT NULL, `finalTitle` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `editorial_reviews` (`id` TEXT NOT NULL, `candidateType` TEXT NOT NULL, `candidateId` TEXT NOT NULL, `decision` TEXT NOT NULL, `note` TEXT NOT NULL, `reviewedBy` TEXT NOT NULL, `reviewedAt` INTEGER NOT NULL, `articleIds` TEXT NOT NULL, `sourceIds` TEXT NOT NULL, `languages` TEXT NOT NULL, `sharedEntities` TEXT, `confidence` TEXT, `method` TEXT NOT NULL, `rationale` TEXT NOT NULL, `uncertaintyReasons` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `feed_metadata` (`id` TEXT NOT NULL, `lastSuccessfulFetch` INTEGER, `lastAttemptedFetch` INTEGER, `successfulSourceCount` INTEGER NOT NULL, `failedSourceCount` INTEGER NOT NULL, `totalArticleCount` INTEGER NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `event_clusters` (`id` TEXT NOT NULL, `eventSummary` TEXT NOT NULL, `eventTime` INTEGER NOT NULL, `clusteredAt` INTEGER NOT NULL, `confidence` TEXT NOT NULL, `groupingExplanation` TEXT NOT NULL, `commonEntities` TEXT NOT NULL, `publisherCount` INTEGER NOT NULL, `imageUrl` TEXT, `articleIds` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `source_health` (`sourceId` TEXT NOT NULL, `sourceName` TEXT NOT NULL, `status` TEXT NOT NULL, `lastSuccessAt` INTEGER, `lastFailureAt` INTEGER, `consecutiveFailures` INTEGER NOT NULL, `last24hSuccessRate` REAL NOT NULL, `last24hArticleCount` INTEGER NOT NULL, `lastErrorMessage` TEXT, `lastErrorCategory` TEXT, `disabledReason` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`sourceId`))""",
        """CREATE TABLE IF NOT EXISTS `event_integrity` (`clusterId` TEXT NOT NULL, `distinctPublisherCount` INTEGER NOT NULL, `articleCount` INTEGER NOT NULL, `timeWindowHours` INTEGER NOT NULL, `commonNamedEntities` TEXT NOT NULL, `headlineSimilarityScores` TEXT NOT NULL, `averageHeadlineSimilarity` REAL NOT NULL, `sharedEntityCounts` TEXT NOT NULL, `sourceIds` TEXT NOT NULL, `articleUrls` TEXT NOT NULL, `confidence` TEXT NOT NULL, `matchRationale` TEXT NOT NULL, `clusteredAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`clusterId`))"""
    )
}
