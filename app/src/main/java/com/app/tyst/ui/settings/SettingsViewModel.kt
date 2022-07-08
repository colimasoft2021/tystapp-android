package com.app.tyst.ui.settings

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.app.tyst.BuildConfig
import com.app.tyst.MainApplication
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.PurchaseSubscriptionRequest
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.ui.settings.inappbilling.BillingRepository
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.helper.LOGApp
import com.google.gson.JsonElement

/**
 * View model call for performing all business logic of [com.app.tyst.ui.settings.SettingsFragment]
 */
class SettingsViewModel(app: Application) : BaseViewModel(app) {

    private val LOG_TAG = "BillingViewModel"

    var notificationSettingData = MutableLiveData<WSObserverModel<JsonElement>>()
    var logOutLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var deleteAccountData = MutableLiveData<WSObserverModel<JsonElement>>()
//    var subsriptionPuchaseData = MutableLiveData<WSObserverModel<JsonElement>>()
//    var addFreeSKU = MutableLiveData<SkuDetails>()
//    var purchaseData = MutableLiveData<PurchaseSubscriptionRequest>()
//    private val repository: BillingRepository
    var settingConfig: SettingViewConfig

    init {
//        repository = BillingRepository.getInstance(app)
//        repository.startDataSourceConnections()
//        addFreeSKU = repository.addFreeSKU
//        purchaseData = repository.purchaseData
        settingConfig = setUpSettingConfig()
    }

    override fun onCleared() {
        super.onCleared()
        LOGApp.d(LOG_TAG, "onCleared")
//        repository.endDataSourceConnections()
    }

    /**
     * Set visibility of settings
     * If user skip login, then notification, edit profile, delete account, send feedback and logout will hide
     * @return SettingViewConfig
     */
    private fun setUpSettingConfig(): SettingViewConfig {
        val user = sharedPreference.userDetail
        return SettingViewConfig().apply {
            showNotification = false
            showRemoveAdd = ((AppConfig.BANNER_AD || AppConfig.INTERSTITIAL_AD) && (sharedPreference.userDetail?.isSubscripbed() == false) &&!sharedPreference.isSkip)
            showEditProfile = !sharedPreference.isSkip
            showChangePassword = !(user?.isSocialLogin() == true || sharedPreference.isSkip)
            showChangePhone = ((app as MainApplication).getApplicationLoginType().equals(IConstants.LOGIN_TYPE_PHONE, true) ||
                    (app).getApplicationLoginType().equals(IConstants.LOGIN_TYPE_PHONE_SOCIAL, true)) && !sharedPreference.isSkip
            showDeleteAccount = !sharedPreference.isSkip
            showSendFeedback = !sharedPreference.isSkip
            showLogOut = !sharedPreference.isSkip
            appVersion = "Version: " + BuildConfig.VERSION_NAME
        }
    }

//    fun makePurchase(activity: Activity?, skuDetails: SkuDetails?) {
//        if (activity != null && skuDetails != null)
//            repository.launchBillingFlow(activity, skuDetails = skuDetails)
//    }

    /**
     * Api call for manage notification setting
     */
    fun callManageNotification(disableNotification: String) {
        val map = HashMap<String, String>()
        map["notification"] = disableNotification
        SettingRepository(this).callManageNotification(map = map, notificationSettingData = notificationSettingData)
    }

    /**
     * Api call for log out
     */
    fun callLogOut() {
        SettingRepository(this).callLogOut(logOutLiveData)
    }

    /**
     * Api call for delete account
     */
    fun callDeleteAccount() {
        SettingRepository(this).callDeleteAccount(deleteAccountData)
    }

    /**
     * Clear all user saved data
     */
    fun performLogout() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
    }

    /**
     * Clear all user saved data
     */
    fun updateNotificationSetting(setting: String) {
        val userDetail = sharedPreference.userDetail
        userDetail?.notification = setting
        sharedPreference.userDetail = userDetail

    }

    /**
     * Api call for send transaction data on server
     * @param receiptData In-App purchase receipt data
     */
    /*fun callSubscriptionPurchase(receiptData: PurchaseSubscriptionRequest) {
        val map = HashMap<String, String>()
        map["subscription_id"] = receiptData.subscriptionId
        map["purchase_token"] = receiptData.purchaseToken
        map["receipt_type"] = receiptData.receiptType
        SettingRepository(this).callSubscriptionPurchase(map = map, subsriptionPuchaseData = subsriptionPuchaseData)
    }*/
}