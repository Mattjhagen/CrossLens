package com.crosslens.app.data.translation

import android.app.Application
import androidx.room.Room
import com.crosslens.app.core.model.Article
import com.crosslens.app.core.model.TranslationStatus
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.entity.ArticleEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CachedTranslationRepositoryTest {
    private lateinit var db: CrossLensDatabase
    private val article = ArticleEntity("article", "story", "publisher", "https://publisher.example/1",
        Instant.parse("2026-10-31T12:00:00Z"), "fr", "Titre", "Extrait", "Extrait", "Publisher", false)
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CrossLensDatabase::class.java).allowMainThreadQueries().build() }
    @After fun close() = db.close()
    @Test fun `unconfigured provider preserves original and returns explicit unavailable`() = runTest {
        db.articleDao().insertArticles(listOf(article))
        val translation = CachedTranslationRepository(db, UnconfiguredTranslationProvider()).getTranslation("article", "en")!!
        assertEquals(TranslationStatus.UNAVAILABLE, translation.status)
        assertNull(translation.translatedHeadline)
        assertEquals("Titre", db.articleDao().getArticlesByIds(listOf("article")).single().originalHeadline)
    }
    @Test fun `cache is invalidated when original reporting changes`() = runTest {
        db.articleDao().insertArticles(listOf(article))
        var calls = 0
        val provider = object : TranslationProvider {
            override val providerId = "test-fixture-provider"
            override suspend fun translate(article: Article, targetLanguage: String): TranslationOutput {
                calls++
                return TranslationOutput(TranslationStatus.AVAILABLE, "Test translation", "Test excerpt", "test-only")
            }
        }
        val repository = CachedTranslationRepository(db, provider)
        val first = repository.getTranslation("article", "en")!!
        repository.getTranslation("article", "en")
        assertEquals(1, calls)
        db.articleDao().insertArticles(listOf(article.copy(originalExcerpt = "Revised excerpt")))
        val second = repository.getTranslation("article", "en")!!
        assertEquals(2, calls)
        assertNotEquals(first.originalContentRevision, second.originalContentRevision)
        assertEquals(1, db.translationDao().getTranslationsByArticleIds(listOf("article")).size)
    }
    @Test fun `translation failures never overwrite the original`() = runTest {
        db.articleDao().insertArticles(listOf(article))
        val provider = object : TranslationProvider {
            override val providerId = "test"
            override suspend fun translate(article: Article, targetLanguage: String): TranslationOutput = error("Unavailable")
        }
        assertEquals(TranslationStatus.FAILED, CachedTranslationRepository(db, provider).getTranslation("article", "en")!!.status)
        assertTrue(db.translationDao().getTranslationsByArticleIds(listOf("article")).isEmpty())
    }
    @Test fun `translation cancellation propagates`() = runTest {
        db.articleDao().insertArticles(listOf(article))
        val provider = object : TranslationProvider {
            override val providerId = "test"
            override suspend fun translate(article: Article, targetLanguage: String): TranslationOutput = throw CancellationException("Canceled")
        }
        try { CachedTranslationRepository(db, provider).getTranslation("article", "en"); fail("Must cancel") }
        catch (_: CancellationException) { }
    }
}
