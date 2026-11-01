package com.crosslens.app.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.crosslens.app.core.model.AccessTier
import com.crosslens.app.data.preferences.DataStoreEntitlementRepository
import com.crosslens.app.data.preferences.PlusFeature
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Integration test that verifies entitlement changes persist across "app restarts"
 * by creating new repository instances that read from the same DataStore file.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsEntitlementIntegrationTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var testDataStoreFile: File
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        testDataStoreFile = tmpFolder.newFile("test_prefs.preferences_pb")
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
    }

    @After
    fun teardown() {
        testScope.cancel()
    }

    private fun createDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testDataStoreFile }
        )
    }

    @Test
    fun `free promotion survives legacy writes and recreation without changing stored tier`() = runTest(testDispatcher) {
        val dataStore = createDataStore()
        for (legacyTier in listOf(AccessTier.FREE, AccessTier.PLUS_DEMO)) {
            val repository = DataStoreEntitlementRepository(dataStore)
            repository.setAccessTier(legacyTier)
            val recreated = DataStoreEntitlementRepository(dataStore)
            assertEquals(AccessTier.PLUS_FREE, recreated.entitlementFlow.first().activeTier)
            assertEquals("free_promotion", recreated.entitlementFlow.first().accessStateSource)
            for (feature in PlusFeature.values()) assertTrue(recreated.hasAccess(feature).first())
            val stored = dataStore.data.first()[androidx.datastore.preferences.core.stringPreferencesKey("access_tier")]
            assertEquals(legacyTier.name, stored)
        }
    }
}
