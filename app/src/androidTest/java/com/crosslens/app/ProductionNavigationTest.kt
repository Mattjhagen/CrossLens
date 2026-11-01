package com.crosslens.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crosslens.app.data.local.CrossLensDatabase
import com.crosslens.app.data.mock.MockStoryRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/** Deterministic offline navigation: no live network or billing service is needed. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductionNavigationTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()
    @Inject lateinit var database: CrossLensDatabase
    @Inject lateinit var fallback: MockStoryRepository
    private lateinit var title: String
    @Before fun setup() = runBlocking {
        hilt.inject()
        fallback.seedData()
        title = database.storyDao().getAllStories().first().title
    }
    @Test fun offlineStorySaveCompareAndBack() {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText(title).onFirst().performClick()
        val save = compose.activity.getString(R.string.save_story)
        val unsave = compose.activity.getString(R.string.unsave_story)
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription(save).fetchSemanticsNodes().isNotEmpty() ||
            compose.onAllNodesWithContentDescription(unsave).fetchSemanticsNodes().isNotEmpty() }
        if (compose.onAllNodesWithContentDescription(save).fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithContentDescription(save).performClick()
        compose.onNodeWithContentDescription(unsave).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.compare_perspectives)).performScrollTo().performClick()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.cd_back)).performClick()
        compose.onNodeWithContentDescription(unsave).assertExists()
    }
}
