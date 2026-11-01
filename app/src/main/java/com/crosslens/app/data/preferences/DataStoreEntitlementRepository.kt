package com.crosslens.app.data.preferences

import com.crosslens.app.BuildConfig
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
            activeTier = PreviewEntitlementPolicy.resolve(tierName, BuildConfig.DEBUG),
            accessStateSource = if (BuildConfig.DEBUG) "local_demo" else "billing_unavailable",
            updateTime = Instant.now()
        )
    }

    override suspend fun setAccessTier(tier: AccessTier) {
        dataStore.edit { it[Keys.ACCESS_TIER] = if (BuildConfig.DEBUG) tier.name else AccessTier.FREE.name }
    }

    override fun hasAccess(feature: PlusFeature): Flow<Boolean> {
        return entitlementFlow.map { it.activeTier == AccessTier.PLUS_DEMO }
    }
}

/** Preview state is never proof of a production purchase. */
object PreviewEntitlementPolicy {
    fun resolve(storedTier: String?, previewAllowed: Boolean): AccessTier =
        if (previewAllowed) AccessTier.values().firstOrNull { it.name == storedTier } ?: AccessTier.FREE
        else AccessTier.FREE
}
