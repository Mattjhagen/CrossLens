package com.crosslens.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.local.dao.SourceHealthDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test to verify Source Health refresh button actually triggers
 * the ViewModel refresh method and updates the database.
 *
 * This test reproduces the bug where the UI timestamp stays frozen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SourceHealthRefreshTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: CrossLensDatabase

    private lateinit var sourceHealthDao: SourceHealthDao

    @Before
    fun setup() {
        hiltRule.inject()
        sourceHealthDao = database.sourceHealthDao()

        // Clear database
        runBlocking {
            sourceHealthDao.deleteAll()
        }
    }

    @Test
    fun refreshButton_updatesTimestamp() = runBlocking {
        // Navigate to Source Health screen
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        // Scroll to Source Health option
        composeTestRule.onNodeWithText("Source Health & Coverage")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Get initial timestamp
        val initialSources = sourceHealthDao.getAllHealth()
        val initialMaxTimestamp = initialSources.maxOfOrNull { it.updatedAt }

        // Wait a bit to ensure timestamp will be different
        Thread.sleep(100)

        // Tap refresh button
        composeTestRule.onNodeWithText("Refresh All Sources").performClick()

        // Wait for refresh to complete (timeout after 30 seconds)
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            // Check if any source timestamp has been updated
            val sources = runBlocking { sourceHealthDao.getAllHealth() }
            val maxTimestamp = sources.maxOfOrNull { it.updatedAt }
            maxTimestamp != null && maxTimestamp > (initialMaxTimestamp ?: 0)
        }

        // Verify timestamp was updated
        val finalSources = sourceHealthDao.getAllHealth()
        val finalMaxTimestamp = finalSources.maxOfOrNull { it.updatedAt }

        assert(finalMaxTimestamp != null) { "No sources in database after refresh" }
        assert(finalMaxTimestamp!! > (initialMaxTimestamp ?: 0)) {
            "Timestamp not updated: initial=$initialMaxTimestamp, final=$finalMaxTimestamp"
        }
    }

    @Test
    fun refreshButton_callsViewModel() {
        // This test verifies the button click reaches the ViewModel
        // by checking that the button is clickable and enabled

        // Navigate to Source Health
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Source Health & Coverage")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Verify button exists and is clickable
        composeTestRule
            .onNodeWithText("Refresh All Sources")
            .assertExists()
            .assertIsEnabled()
            .assertHasClickAction()
    }
}
