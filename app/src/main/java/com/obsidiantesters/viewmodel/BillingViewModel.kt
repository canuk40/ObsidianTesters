package com.obsidiantesters.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.google.firebase.auth.FirebaseAuth
import com.obsidiantesters.data.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    fun connectAndLoad() {
        viewModelScope.launch {
            val connected = billingRepository.connect()
            if (connected) {
                _products.value = billingRepository.queryProductDetails()
                processPendingPurchases()
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchBillingFlow(activity, productDetails)
    }

    private fun processPendingPurchases() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val purchases = billingRepository.queryActivePurchases()
            purchases.forEach { purchase ->
                billingRepository.processPurchase(purchase, userId).onFailure { e ->
                    _billingError.value = e.message
                }
            }
        }
    }
}
