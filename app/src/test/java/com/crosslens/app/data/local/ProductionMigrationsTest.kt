package com.crosslens.app.data.local

import android.app.Application
import android.content.ContentValues
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Fixtures are reconstructed from Git history. Actual distributed beta databases still need upgrade QA. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ProductionMigrationsTest {
    @Test fun `evidenced historical versions migrate through actual Room validation without losing articles`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        ProductionMigrations.supportedStartVersions.forEach { version ->
            val name = "migration-$version"
            context.deleteDatabase(name)
            val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name).callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        val sql = requireNotNull(javaClass.classLoader?.getResourceAsStream("schema-history/$version.sql"))
                            .bufferedReader().use { it.readText() }
                        sql.lineSequence().filterNot { it.trim().startsWith("--") }.joinToString("\n")
                            .split(';').filter { it.isNotBlank() }.forEach { db.execSQL(it) }
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = error("Fixture only")
                }).build())
            helper.writableDatabase.insert("articles", 0, ContentValues().apply {
                put("id", "preserved-article"); put("storyId", "preserved-story"); put("sourceId", "preserved-source")
                put("originalUrl", "https://publisher.example/original"); put("publishedTime", 1000L)
                put("originalLanguage", "fr"); put("originalHeadline", "Original headline"); put("originalExcerpt", "Original excerpt")
                put("attribution", "Publisher"); put("isDemo", 0)
                if (version >= 4) { put("originalContent", "Original retained content"); put("requiresSubscription", 0) }
                if (version >= 8) put("imageUrl", "https://publisher.example/image.jpg")
            })
            helper.close()
            val database = Room.databaseBuilder(context, CrossLensDatabase::class.java, name)
                .addMigrations(*ProductionMigrations.ALL).build()
            try {
                // Opening executes Room's complete schema validation, not just our SQL assertions.
                database.openHelper.writableDatabase
                val article = database.articleDao().getArticlesByIds(listOf("preserved-article")).single()
                assertEquals("Original headline", article.originalHeadline)
                assertEquals("Original excerpt", article.originalExcerpt)
                assertEquals(if (version < 4) "Original excerpt" else "Original retained content", article.originalContent)
                assertEquals("https://publisher.example/original", article.originalUrl)
                if (version >= 8) assertEquals("https://publisher.example/image.jpg", article.imageUrl)
            } finally { database.close(); context.deleteDatabase(name) }
        }
    }
    @Test fun `unknown schema seven is not assigned an invented migration`() {
        assertFalse(7 in ProductionMigrations.supportedStartVersions)
    }
}
