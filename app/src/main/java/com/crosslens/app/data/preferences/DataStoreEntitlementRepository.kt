package com.crosslens.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crosslens.app.core.model.AccessTier
import com.crosslens.app.core.model.Entitlement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreEntitlementRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : EntitlementRepository {

    private object Keys {
        val ACCESS_TIER = stringPreferencesKey("access_tier")
    }

    override val entitlementFlow: Flow<Entitlement> = dataStore.data.map { prefs ->
        val tierName = prefs[Keys.ACCESS_TIER] ?: AccessTier.FREE.name
        Entitlement(
            activeTier = AccessTier.valueOf(tierName),
            accessStateSource = "local_demo",
            updateTime = Instant.now()
        )
    }

    override suspend fun setAccessTier(tier: AccessTier) {
        dataStore.edit { it[Keys.ACCESS_TIER] = tier.name }
    }

    override fun hasAccess(feature: PlusFeature): Flow<Boolean> {
        return entitlementFlow.map { it.activeTier == AccessTier.PLUS_DEMO }
    }
}
