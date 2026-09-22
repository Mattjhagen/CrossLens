package com.crosslens.app.core.model

import java.time.Instant

data class Entitlement(
    val activeTier: AccessTier,
    val accessStateSource: String, // e.g., "local_demo", "play_billing"
    val updateTime: Instant
)

enum class AccessTier {
    FREE,
    PLUS_DEMO // Local preview only, not a real subscription
}
