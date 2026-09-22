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

    @Test
    fun `default entitlement is FREE`() = runTest {
        whenever(dataStore.data).thenReturn(flowOf(preferencesOf()))
        repository = DataStoreEntitlementRepository(dataStore)

        val entitlement = repository.entitlementFlow.first()
        assertEquals(AccessTier.FREE, entitlement.activeTier)
    }

    @Test
    fun `hasAccess returns false for FREE tier`() = runTest {
        whenever(dataStore.data).thenReturn(flowOf(preferencesOf()))
        repository = DataStoreEntitlementRepository(dataStore)

        val hasAccess = repository.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertEquals(false, hasAccess)
    }

    @Test
    fun `hasAccess returns true for PLUS_DEMO tier`() = runTest {
        val prefs = preferencesOf(
            stringPreferencesKey("access_tier") to AccessTier.PLUS_DEMO.name
        )
        whenever(dataStore.data).thenReturn(flowOf(prefs))
        repository = DataStoreEntitlementRepository(dataStore)

        val hasAccess = repository.hasAccess(PlusFeature.ALL_SOURCES).first()
        assertEquals(true, hasAccess)
    }
}
