package com.crosslens.app.data.preferences

import com.crosslens.app.core.model.DimensionType
import com.crosslens.app.core.model.PersonalRelevancePreference
import com.crosslens.app.core.model.PreferenceType
import kotlinx.coroutines.flow.Flow

interface PersonalRelevanceRepository {
    val preferencesFlow: Flow<List<PersonalRelevancePreference>>

    suspend fun addPreference(
        preferenceType: PreferenceType,
        dimensionType: DimensionType,
        dimensionValue: String
    )

    suspend fun removePreference(preferenceId: String)

    suspend fun clearAll()

    suspend fun getPreferences(): List<PersonalRelevancePreference>
}
