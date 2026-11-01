package com.crosslens.app.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.coroutines.resume

sealed interface BillingState {
    data object Disconnected : BillingState
    data object Connecting : BillingState
    data class Ready(val products: List<ProductDetails>) : BillingState
    data object Pending : BillingState
    data object Verifying : BillingState
    data class Verified(val subscription: VerifiedSubscription) : BillingState
    data object NoPurchases : BillingState
    data class Failure(val message: String) : BillingState
}

/** Inactive integration: intentionally not bound in DI until products and a trusted verifier exist.
 * Owner must close this connection with its lifecycle. Supplied scope must be lifecycle-owned.
 * No local purchase flag is ever treated as an entitlement.
 */
class PlayBillingGateway(
    context: Context,
    private val configuration: BillingConfiguration,
    private val verifier: PurchaseVerifier,
    private val scope: CoroutineScope
) : AutoCloseable {
    private val mutableState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val state = mutableState.asStateFlow()
    private val verificationMutex = Mutex()
    private var verificationJob: Job? = null
    @Volatile private var closed = false
    private val client = BillingClient.newBuilder(context.applicationContext)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans().build())
        .enableAutoServiceReconnection()
        .setListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty())
                BillingClient.BillingResponseCode.USER_CANCELED -> queryProducts()
                else -> mutableState.value = BillingState.Failure("Purchase could not be completed. Restore to check your status.")
            }
        }.build()

    fun connect() {
        if (closed) return
        if (client.isReady) { restore(); return }
        if (mutableState.value == BillingState.Connecting) return
        mutableState.value = BillingState.Connecting
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (closed) return
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { queryProducts(); restore() }
                else mutableState.value = BillingState.Failure("Google Play Billing is unavailable.")
            }
            override fun onBillingServiceDisconnected() {
                if (!closed) mutableState.value = BillingState.Disconnected
            }
        })
    }

    private fun queryProducts() {
        if (closed || !client.isReady) return
        val params = QueryProductDetailsParams.newBuilder().setProductList(configuration.productIds.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
        }).build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (!closed && result.responseCode == BillingClient.BillingResponseCode.OK &&
                mutableState.value !is BillingState.Verified && mutableState.value != BillingState.Verifying) {
                mutableState.value = BillingState.Ready(details.productDetailsList)
            }
        }
    }

    fun restore() {
        if (closed) return
        if (!client.isReady) { connect(); return }
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { result, purchases ->
            if (closed) return@queryPurchasesAsync
            if (result.responseCode == BillingClient.BillingResponseCode.OK) processPurchases(purchases)
            else mutableState.value = BillingState.Failure("Restore failed. Retry when Google Play is available.")
        }
    }

    /** UI must show Play-provided price, billing period, renewal, trial and cancellation disclosures first. */
    fun purchase(activity: Activity, product: ProductDetails, offerToken: String) {
        if (closed || !client.isReady || product.productId !in configuration.productIds ||
            product.subscriptionOfferDetails.orEmpty().none { it.offerToken == offerToken }) {
            mutableState.value = BillingState.Failure("Subscription offer unavailable. Refresh and try again.")
            return
        }
        val item = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).setOfferToken(offerToken).build()
        val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(item)).build())
        if (result.responseCode != BillingClient.BillingResponseCode.OK)
            mutableState.value = BillingState.Failure("Google Play could not open the purchase flow.")
    }

    private fun processPurchases(purchases: List<Purchase>) {
        if (closed) return
        verificationJob?.cancel()
        verificationJob = scope.launch {
            verificationMutex.withLock {
                val owned = purchases.filter { purchase -> purchase.products.any { it in configuration.productIds } }
                if (owned.isEmpty()) { mutableState.value = BillingState.NoPurchases; return@withLock }
                val purchased = owned.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (purchased.isEmpty()) { mutableState.value = BillingState.Pending; return@withLock }
                mutableState.value = BillingState.Verifying
                try {
                    val decisions = withContext(Dispatchers.IO) {
                        withTimeout(15_000) { purchased.map { it to verifier.verify(it.purchaseToken, it.products) } }
                    }
                    val eligible = decisions.filter { SubscriptionAccessPolicy.grantsAccess(it.second, configuration, Instant.now()) }
                        .maxByOrNull { it.second.expiresAt }
                    if (eligible == null) { mutableState.value = BillingState.NoPurchases; return@withLock }
                    val (purchase, decision) = eligible
                    // Only acknowledge after trusted server verification. Server should also retry acknowledgement
                    // and reconcile renewals/revocations using RTDN; a client callback is not durable processing.
                    val acknowledged = purchase.isAcknowledged || withTimeoutOrNull(10_000) {
                        suspendCancellableCoroutine<Boolean> { continuation ->
                            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()) { result ->
                                if (continuation.isActive) continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                            }
                        }
                    } == true
                    mutableState.value = if (acknowledged && SubscriptionAccessPolicy.grantsAccess(decision, configuration, Instant.now()))
                        BillingState.Verified(decision)
                    else BillingState.Failure("Purchase verification or acknowledgement needs retry. Restore purchases.")
                } catch (e: TimeoutCancellationException) {
                    ensureActive()
                    mutableState.value = BillingState.Failure("Verification timed out. Restore purchases to retry.")
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    mutableState.value = BillingState.Failure("Purchase verification unavailable. Restore purchases to retry.")
                }
            }
        }
    }

    override fun close() {
        closed = true
        verificationJob?.cancel()
        client.endConnection()
        mutableState.value = BillingState.Disconnected
    }
}
