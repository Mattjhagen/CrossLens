package com.crosslens.app.data.billing

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class SubscriptionAccessPolicyTest {
    private val now = Instant.parse("2026-11-01T12:00:00Z")
    private val config = BillingConfiguration(setOf("plus"), "https://backend.example/verify")
    private fun subscription(status: SubscriptionStatus) = VerifiedSubscription("com.crosslens.app", "plus", status, now.plusSeconds(3600), now)
    @Test fun `active grace and canceled access lasts only through paid expiry`() {
        listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.IN_GRACE_PERIOD, SubscriptionStatus.CANCELED).forEach {
            assertTrue(SubscriptionAccessPolicy.grantsAccess(subscription(it), config, now))
            assertFalse(SubscriptionAccessPolicy.grantsAccess(subscription(it).copy(expiresAt = now), config, now))
        }
    }
    @Test fun `pending held paused expired and revoked never grant access`() {
        listOf(SubscriptionStatus.PENDING, SubscriptionStatus.ON_HOLD, SubscriptionStatus.PAUSED, SubscriptionStatus.EXPIRED, SubscriptionStatus.REVOKED).forEach {
            assertFalse(SubscriptionAccessPolicy.grantsAccess(subscription(it), config, now))
        }
    }
    @Test fun `wrong package product stale and future verification fail closed`() {
        val valid = subscription(SubscriptionStatus.ACTIVE)
        listOf(valid.copy(packageName = "other"), valid.copy(productId = "other"),
            valid.copy(verifiedAt = now.minusSeconds(86401)), valid.copy(verifiedAt = now.plusSeconds(1))).forEach {
            assertFalse(SubscriptionAccessPolicy.grantsAccess(it, config, now))
        }
    }
}
