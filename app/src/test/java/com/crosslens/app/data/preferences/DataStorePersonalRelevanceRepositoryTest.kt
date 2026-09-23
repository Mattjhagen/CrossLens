package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.crosslens.app.core.model.DimensionType
import com.crosslens.app.core.model.PreferenceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStorePersonalRelevanceRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var repository: DataStorePersonalRelevanceRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    @Before
    fun setup() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_preferences.preferences_pb") }
        )
        repository = DataStorePersonalRelevanceRepository(testDataStore)
    }

    @After
    fun tearDown() {
        // No need to cancel - runTest handles cleanup
    }

    @Test
    fun `addPreference stores new preference`() = runTest {
        // When
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        // Then
        val preferences = repository.preferencesFlow.first()
        assertEquals(1, preferences.size)
        assertEquals(PreferenceType.MORE, preferences[0].preferenceType)
        assertEquals(DimensionType.TOPIC, preferences[0].dimensionType)
        assertEquals("technology", preferences[0].dimensionValue)
    }

    @Test
    fun `addPreference replaces existing preference for same dimension value`() = runTest {
        // Given - add a MORE preference
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        // When - add a LESS preference for the same dimension value
        repository.addPreference(
            preferenceType = PreferenceType.LESS,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        // Then - only one preference exists with the new type
        val preferences = repository.preferencesFlow.first()
        assertEquals(1, preferences.size)
        assertEquals(PreferenceType.LESS, preferences[0].preferenceType)
        assertEquals("technology", preferences[0].dimensionValue)
    }

    @Test
    fun `addPreference allows multiple different topics`() = runTest {
        // When
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "environment"
        )

        // Then
        val preferences = repository.preferencesFlow.first()
        assertEquals(2, preferences.size)
        assertTrue(preferences.any { it.dimensionValue == "technology" })
        assertTrue(preferences.any { it.dimensionValue == "environment" })
    }

    @Test
    fun `addPreference allows different dimension types`() = runTest {
        // When
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.REGION,
            dimensionValue = "europe"
        )

        // Then
        val preferences = repository.preferencesFlow.first()
        assertEquals(2, preferences.size)
        assertTrue(preferences.any { it.dimensionType == DimensionType.TOPIC })
        assertTrue(preferences.any { it.dimensionType == DimensionType.REGION })
    }

    @Test
    fun `removePreference deletes specific preference`() = runTest {
        // Given
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )
        repository.addPreference(
            preferenceType = PreferenceType.LESS,
            dimensionType = DimensionType.REGION,
            dimensionValue = "europe"
        )

        val preferences = repository.preferencesFlow.first()
        val idToRemove = preferences.first { it.dimensionValue == "technology" }.id

        // When
        repository.removePreference(idToRemove)

        // Then
        val remaining = repository.preferencesFlow.first()
        assertEquals(1, remaining.size)
        assertEquals("europe", remaining[0].dimensionValue)
    }

    @Test
    fun `clearAll removes all preferences`() = runTest {
        // Given
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )
        repository.addPreference(
            preferenceType = PreferenceType.LESS,
            dimensionType = DimensionType.REGION,
            dimensionValue = "europe"
        )

        // When
        repository.clearAll()

        // Then
        val preferences = repository.preferencesFlow.first()
        assertTrue(preferences.isEmpty())
    }

    @Test
    fun `preferences persist across repository recreation`() = runTest {
        // Given
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        // When - create new repository instance with same DataStore
        val newRepository = DataStorePersonalRelevanceRepository(testDataStore)

        // Then
        val preferences = newRepository.preferencesFlow.first()
        assertEquals(1, preferences.size)
        assertEquals("technology", preferences[0].dimensionValue)
    }

    @Test
    fun `reversibility - changing preference type updates existing`() = runTest {
        // Given - user saves "more like this"
        repository.addPreference(
            preferenceType = PreferenceType.MORE,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        var preferences = repository.preferencesFlow.first()
        assertEquals(1, preferences.size)
        assertEquals(PreferenceType.MORE, preferences[0].preferenceType)

        // When - user changes to "less like this"
        repository.addPreference(
            preferenceType = PreferenceType.LESS,
            dimensionType = DimensionType.TOPIC,
            dimensionValue = "technology"
        )

        // Then - preference is updated, not duplicated
        preferences = repository.preferencesFlow.first()
        assertEquals(1, preferences.size)
        assertEquals(PreferenceType.LESS, preferences[0].preferenceType)
        assertEquals("technology", preferences[0].dimensionValue)
    }
}
