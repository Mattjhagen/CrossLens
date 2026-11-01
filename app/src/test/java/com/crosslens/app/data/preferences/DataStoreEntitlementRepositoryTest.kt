package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crosslens.app.core.model.AccessTier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.*

class DataStoreEntitlementRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreEntitlementRepository

    @Before
    fun setup() {
        dataStore = mock()
    }

    @Test fun `release refuses stored preview and malformed tiers fail closed`() {
        assertEquals(AccessTier.FREE, PreviewEntitlementPolicy.resolve("PLUS_DEMO", false))
        assertEquals(AccessTier.FREE, PreviewEntitlementPolicy.resolve("unknown", true))
        assertEquals(AccessTier.FREE, PreviewEntitlementPolicy.resolve("PLUS_FREE", true))
        assertEquals(AccessTier.FREE, PreviewEntitlementPolicy.resolve(null, true))
    }

    @Test fun `all features are free for fresh legacy and malformed states in both builds`() = runTest {
        for (stored in listOf(null, "FREE", "PLUS_DEMO", "unknown")) {
            for (debug in listOf(false, true)) {
                assertEquals(AccessTier.PLUS_FREE, PremiumAccessPolicy.resolve(stored, debug))
            }
            val prefs = stored?.let { preferencesOf(stringPreferencesKey("access_tier") to it) }
                ?: preferencesOf()
            whenever(dataStore.data).thenReturn(flowOf(prefs))
            repository = DataStoreEntitlementRepository(dataStore)
            assertEquals("free_promotion", repository.entitlementFlow.first().accessStateSource)
            for (feature in PlusFeature.values()) {
                assertTrue(repository.hasAccess(feature).first())
            }
        }
    }
}
