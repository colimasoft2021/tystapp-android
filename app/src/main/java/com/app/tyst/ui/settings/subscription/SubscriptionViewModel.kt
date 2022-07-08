package com.app.tyst.ui.settings.subscription

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.PurchaseSubscriptionRequest
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.ui.settings.SettingRepository
import com.app.tyst.ui.settings.inappbilling.BillingRepository
import com.app.tyst.utility.helper.LOGApp
import com.google.gson.JsonElement

/**
 * View model call for performing all business logic of [com.app.tyst.ui.settings.subscription.SubscriptionActivity]
 */

class SubscriptionViewModel (app: Application) : BaseViewModel(app) {

    private val LOG_TAG = "BillingViewModel"

    var subsriptionPuchaseData = MutableLiveData<WSObserverModel<JsonElement>>()
    var monthlySubscriptionSKU = MutableLiveData<SkuDetails>()
    var purchaseData = MutableLiveData<PurchaseSubscriptionRequest>()
    val repository: BillingRepository

    init {
        repository = BillingRepository.getInstance(app)
        repository.startDataSourceConnections()
        monthlySubscriptionSKU = repository.addFreeSKU
        purchaseData = repository.purchaseData
    }

    override fun onCleared() {
        super.onCleared()
        LOGApp.d(LOG_TAG, "onCleared")
        repository.endDataSourceConnections()
    }

    fun makePurchase(activity: Activity?, skuDetails: SkuDetails?) {
        if (activity != null && skuDetails != null)
            repository.launchBillingFlow(activity, skuDetails = skuDetails)
    }

    /**
     * Api call for send transaction data on server
     * @param receiptData In-App purchase receipt data
     */
    fun callSubscriptionPurchase(receiptData: PurchaseSubscriptionRequest) {
        val map = HashMap<String, String>()
        map["subscription_id"] = receiptData.subscriptionId
        map["purchase_token"] = receiptData.purchaseToken
        map["receipt_type"] = receiptData.receiptType
        SettingRepository(this).callSubscriptionPurchase(map = map, subsriptionPuchaseData = subsriptionPuchaseData)
    }
}