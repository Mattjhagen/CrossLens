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
    fun `entitlement defaults to FREE on first launch`() = runTest(testDispatcher) {
        val dataStore = createDataStore()
        val repository = DataStoreEntitlementRepository(dataStore)

        val entitlement = repository.entitlementFlow.first()
        assertEquals(AccessTier.FREE, entitlement.activeTier)
        assertEquals("local_demo", entitlement.accessStateSource)
    }

    @Test
    fun `Settings Preview Plus enables PLUS_DEMO access`() = runTest(testDispatcher) {
        val dataStore = createDataStore()
        val repository = DataStoreEntitlementRepository(dataStore)

        // Initial state: FREE
        val initialEntitlement = repository.entitlementFlow.first()
        assertEquals(AccessTier.FREE, initialEntitlement.activeTier)

        val initialAccess = repository.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertFalse(initialAccess)

        // Simulate Settings → Preview Plus button click
        repository.setAccessTier(AccessTier.PLUS_DEMO)
        testScheduler.advanceUntilIdle()

        // Verify access granted
        val updatedEntitlement = repository.entitlementFlow.first()
        assertEquals(AccessTier.PLUS_DEMO, updatedEntitlement.activeTier)

        val updatedAccess = repository.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertTrue(updatedAccess)
    }

    @Test
    fun `entitlement persists across app restart`() = runTest(testDispatcher) {
        // First launch: enable Plus
        val dataStore = createDataStore()
        val repository1 = DataStoreEntitlementRepository(dataStore)

        repository1.setAccessTier(AccessTier.PLUS_DEMO)
        testScheduler.advanceUntilIdle()

        val entitlement1 = repository1.entitlementFlow.first()
        assertEquals(AccessTier.PLUS_DEMO, entitlement1.activeTier)

        // Simulate app restart: create new repository instance reading same DataStore
        val repository2 = DataStoreEntitlementRepository(dataStore)
        testScheduler.advanceUntilIdle()

        val entitlement2 = repository2.entitlementFlow.first()
        assertEquals(AccessTier.PLUS_DEMO, entitlement2.activeTier)

        val hasAccess = repository2.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertTrue("Plus access should persist after restart", hasAccess)
    }

    @Test
    fun `Reset to Free removes Plus access and persists`() = runTest(testDispatcher) {
        val dataStore = createDataStore()

        // First launch: enable Plus
        val repository1 = DataStoreEntitlementRepository(dataStore)
        repository1.setAccessTier(AccessTier.PLUS_DEMO)
        testScheduler.advanceUntilIdle()
        assertEquals(AccessTier.PLUS_DEMO, repository1.entitlementFlow.first().activeTier)

        // Second launch: reset to Free
        val repository2 = DataStoreEntitlementRepository(dataStore)
        testScheduler.advanceUntilIdle()
        assertEquals(AccessTier.PLUS_DEMO, repository2.entitlementFlow.first().activeTier)

        repository2.setAccessTier(AccessTier.FREE)
        testScheduler.advanceUntilIdle()
        assertEquals(AccessTier.FREE, repository2.entitlementFlow.first().activeTier)

        // Third launch: verify Free persisted
        val repository3 = DataStoreEntitlementRepository(dataStore)
        testScheduler.advanceUntilIdle()

        val entitlement = repository3.entitlementFlow.first()
        assertEquals(AccessTier.FREE, entitlement.activeTier)

        val hasAccess = repository3.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertFalse("Free access should persist after reset", hasAccess)
    }

    @Test
    fun `Plus access unlocks documented features`() = runTest(testDispatcher) {
        val dataStore = createDataStore()
        val repository = DataStoreEntitlementRepository(dataStore)

        // Free tier: no access
        assertFalse(repository.hasAccess(PlusFeature.ALL_SOURCES).first())
        assertFalse(repository.hasAccess(PlusFeature.ALL_TRANSLATIONS).first())
        assertFalse(repository.hasAccess(PlusFeature.FULL_LENS_GAP).first())
        assertFalse(repository.hasAccess(PlusFeature.ADVANCED_FILTERS).first())

        // Enable Plus
        repository.setAccessTier(AccessTier.PLUS_DEMO)
        testScheduler.advanceUntilIdle()

        // Plus tier: all features unlocked
        assertTrue(repository.hasAccess(PlusFeature.ALL_SOURCES).first())
        assertTrue(repository.hasAccess(PlusFeature.ALL_TRANSLATIONS).first())
        assertTrue(repository.hasAccess(PlusFeature.FULL_LENS_GAP).first())
        assertTrue(repository.hasAccess(PlusFeature.ADVANCED_FILTERS).first())
    }
}
