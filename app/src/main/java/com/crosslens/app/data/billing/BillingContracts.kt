package com.crosslens.app.data.billing

import java.time.Duration
import java.time.Instant

/** Product IDs and verification URL are public configuration, never provider credentials. */
data class BillingConfiguration(val productIds: Set<String>, val verificationUrl: String) {
    init {
        require(productIds.isNotEmpty() && productIds.all { it.isNotBlank() })
        val uri = java.net.URI(verificationUrl)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null)
    }
}

enum class SubscriptionStatus { ACTIVE, IN_GRACE_PERIOD, CANCELED, ON_HOLD, PAUSED, EXPIRED, PENDING, REVOKED }

/** Only a trusted authenticated verification response may construct this decision input. */
data class VerifiedSubscription(
    val packageName: String,
    val productId: String,
    val status: SubscriptionStatus,
    val expiresAt: Instant,
    val verifiedAt: Instant
)

object SubscriptionAccessPolicy {
    fun grantsAccess(subscription: VerifiedSubscription, configuration: BillingConfiguration, now: Instant): Boolean =
        subscription.packageName == "com.crosslens.app" && subscription.productId in configuration.productIds &&
            subscription.status in setOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.IN_GRACE_PERIOD, SubscriptionStatus.CANCELED) &&
            subscription.expiresAt.isAfter(now) && Duration.between(subscription.verifiedAt, now).seconds in 0..86400
}

interface PurchaseVerifier {
    /** Backend verifies token with Play Developer API, package/product binding, ownership and current state.
     * Endpoint must authenticate the reader; token and service-account credentials must never be logged.
     * Do not implement by trusting a client-provided boolean or purchase JSON signature alone.
     */
    suspend fun verify(purchaseToken: String, productIds: List<String>): VerifiedSubscription
}
