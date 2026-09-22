package com.crosslens.app.data.preferences

import com.crosslens.app.core.model.AccessTier
import com.crosslens.app.core.model.Entitlement
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    val entitlementFlow: Flow<Entitlement>
    suspend fun setAccessTier(tier: AccessTier)
    fun hasAccess(feature: PlusFeature): Flow<Boolean>
}

enum class PlusFeature {
    ALL_SOURCES,
    ALL_TRANSLATIONS,
    FULL_LENS_GAP,
    ADVANCED_FILTERS
}
