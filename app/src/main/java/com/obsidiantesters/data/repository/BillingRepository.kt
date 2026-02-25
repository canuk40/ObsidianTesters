package com.obsidiantesters.data.repository

import android.app.Activity
import android.util.Base64
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume

/** SKU identifiers */
object BillingProducts {
    const val SKU_BYPASS = "obsidian_testers_bypass"
    const val SKU_PRO_MONTHLY = "obsidian_testers_pro_monthly"
    const val BYPASS_SHARDS = 75
}

@Singleton
class BillingRepository @Inject constructor(
    private val billingClient: BillingClient,
    @Named("playLicenseKey") private val licenseKey: String,
    private val userRepository: UserRepository
) {
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    suspend fun connect(): Boolean = suspendCancellableCoroutine { cont ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                _isConnected.value = result.responseCode == BillingClient.BillingResponseCode.OK
                if (!cont.isCompleted) cont.resume(_isConnected.value)
            }
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    suspend fun queryProductDetails(): List<ProductDetails> {
        val inappProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingProducts.SKU_BYPASS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val subProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingProducts.SKU_PRO_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val inappParams = QueryProductDetailsParams.newBuilder().setProductList(inappProducts).build()
        val subParams = QueryProductDetailsParams.newBuilder().setProductList(subProducts).build()

        val inappResult = billingClient.queryProductDetails(inappParams)
        val subResult = billingClient.queryProductDetails(subParams)

        return (inappResult.productDetailsList ?: emptyList()) +
               (subResult.productDetailsList ?: emptyList())
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): BillingResult {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { offerToken?.let { setOfferToken(it) } }
            .build()

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        return billingClient.launchBillingFlow(activity, params)
    }

    suspend fun queryActivePurchases(): List<Purchase> {
        val inappParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build()
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS).build()

        val inapp = billingClient.queryPurchasesAsync(inappParams).purchasesList
        val subs = billingClient.queryPurchasesAsync(subParams).purchasesList

        val all = inapp + subs
        _purchases.value = all
        return all
    }

    /**
     * Verifies purchase signature via RSA public key loaded from local.properties.
     * Acknowledges and processes the purchase if valid.
     */
    suspend fun processPurchase(purchase: Purchase, userId: String): Result<Unit> = runCatching {
        require(verifySignature(purchase.originalJson, purchase.signature)) {
            "Purchase signature verification failed."
        }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Award based on product
            if (purchase.products.contains(BillingProducts.SKU_BYPASS)) {
                userRepository.creditBypassShards(userId, BillingProducts.BYPASS_SHARDS).getOrThrow()
            }
            if (purchase.products.contains(BillingProducts.SKU_PRO_MONTHLY)) {
                userRepository.updateSubscribedTier(userId, "PRO").getOrThrow()
            }

            // Acknowledge if not already acknowledged
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient.acknowledgePurchase(ackParams)
            }
        }
    }

    private fun verifySignature(signedData: String, signature: String): Boolean {
        if (licenseKey.isBlank()) return false
        return try {
            val keyBytes = Base64.decode(licenseKey, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val key: PublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)
            val sig = Signature.getInstance("SHA1withRSA")
            sig.initVerify(key)
            sig.update(signedData.toByteArray())
            sig.verify(Base64.decode(signature, Base64.DEFAULT))
        } catch (e: Exception) {
            false
        }
    }
}
