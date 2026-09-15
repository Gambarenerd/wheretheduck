package com.whereduck.app.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.data.remote.FirestoreDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID_PREMIUM = "premium_monthly"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
                _isLoading.value = false
            }
            else -> {
                Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
                _purchaseError.value = billingResult.debugMessage
                _isLoading.value = false
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private var isConnected = false

    init {
        connectAndQueryProducts()
    }

    private fun connectAndQueryProducts() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    Log.d(TAG, "Billing connected")
                    scope.launch {
                        queryProductDetails()
                        checkExistingPurchases()
                    }
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
                Log.d(TAG, "Billing disconnected")
            }
        })
    }

    private suspend fun ensureConnected(): Boolean {
        if (isConnected) return true
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    isConnected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    if (cont.isActive) cont.resume(isConnected)
                }

                override fun onBillingServiceDisconnected() {
                    isConnected = false
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    private suspend fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
                Log.d(TAG, "Product details loaded: ${productDetailsList.size} products")
            } else {
                Log.e(TAG, "queryProductDetails failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val details = _productDetails.value
        if (details == null) {
            _purchaseError.value = "Product not available"
            return
        }

        // Get the first offer (base plan)
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            _purchaseError.value = "No offer available"
            return
        }

        _isLoading.value = true

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _isLoading.value = false
            _purchaseError.value = result.debugMessage
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge if not already
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged")
                    } else {
                        Log.e(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                    }
                }
            }

            // Update user plan in Firestore
            val userId = auth.currentUser?.uid
            if (userId != null) {
                try {
                    firestoreDataSource.updateUserPlan(userId, "premium")
                    Log.d(TAG, "User plan updated to premium")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update plan in Firestore", e)
                }
            }
        }

        _isLoading.value = false
    }

    suspend fun checkExistingPurchases() {
        if (!ensureConnected()) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchasesList.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.contains(PRODUCT_ID_PREMIUM)
                }

                val userId = auth.currentUser?.uid ?: return@queryPurchasesAsync
                scope.launch {
                    try {
                        val currentPlan = if (hasActiveSub) "premium" else "free"
                        firestoreDataSource.updateUserPlan(userId, currentPlan)
                        Log.d(TAG, "Synced plan from Play Store: $currentPlan")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync plan", e)
                    }
                }
            }
        }
    }

    fun clearError() {
        _purchaseError.value = null
    }

    fun getFormattedPrice(): String? {
        return _productDetails.value
            ?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
    }
}
