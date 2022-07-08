package com.app.tyst

//import com.crashlytics.android.Crashlytics
//import com.plaid.linkbase.models.PlaidEnvironment
//import com.plaid.linkbase.models.PlaidOptions
//import com.plaid.plog.LogLevel

import android.R
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.local.AppPrefrrences
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.data.model.response.VersionConfigResponse
import com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.SingleLiveEvent
import com.app.tyst.utility.helper.LOGApp
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.plaid.link.Plaid
import com.plaid.linkbase.models.configuration.PlaidEnvironment
import com.plaid.linkbase.models.configuration.PlaidOptions
import com.plaid.log.LogLevel
import netscape.javascript.JSObject.getWindow
import java.lang.ref.WeakReference


/**
 * Created by HB on 21/8/19.
 */
class MainApplication : Application() {

    var weakActivity: WeakReference<BaseActivity>? = null

    var counterIntertitialAdd: Int = 0 // Show interstitial add after 4 count
   // var mInterstitialAd: InterstitialAd? = null
    var addClose = MutableLiveData<Boolean>()
    var isProfileUpdated = MutableLiveData<Boolean>()
    var isAdRemoved = MutableLiveData<Boolean>()
    val stateList = ArrayList<StatesResponse>()
    var newReceiptAdded = MutableLiveData<Boolean>()
    var taxChanged = MutableLiveData<Boolean>()
    var categoryUpdated = MutableLiveData<Boolean>()
    var versionLiveData = SingleLiveEvent<WSObserverModel<VersionConfigResponse>>()

    var isEncryptionApply = true

    companion object {
        lateinit var sharedPreference: AppPrefrrences
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreference = AppPrefrrences(this)
        initAdMob()

        if (BuildConfig.DEBUG) {
            Logger.initializeSession(this)
            Logger.setExceptionCallbackListener(object : Logger.ExceptionCallback {
                override fun onExceptionThrown(exception: Throwable) {
                    Log.e("Crash ", "happen")
                    Crashlytics.logException(exception)
                }
            })
        }

        initPlaidSdk()

        Handler().postDelayed({ HomeRepository(BaseViewModel(this)).callConfigParameters(versionLiveData) }, 5000)

        val crashButton = Button(this)
        crashButton.text = "Test Crash"
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }

        addContentView(crashButton, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    fun setCurrentActivity(activity: BaseActivity) {
        weakActivity = WeakReference(activity)
    }
    fun addContentView(
        view: View?,
        params: ViewGroup.LayoutParams?
    ) {
        this.addContentView(view, params)
    }

        fun getCurrentActivity(): BaseActivity? {
            return weakActivity?.get()
        }

        fun setApplicationLoginType(type: String) {
            sharedPreference.appLoginType = type
        }

        fun getApplicationLoginType(): String {
            return sharedPreference.appLoginType ?: IConstants.LOGIN_TYPE_EMAIL
        }

        /**
         * This will provide application login activity
         * @return Class<*>
         */
        fun getLoginActivity(): Class<*> {
//        return LoginWithEmailActivity::class.java
//        return LoginWithPhoneNumberActivity::class.java
            return LoginWithEmailSocialActivity::class.java
//        return LoginWithPhoneNumberSocialActivity::class.java
        }

        /**
         * Initialize interstitial ads
         */
        private fun initInterstitialAdds() {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd?.adUnitId = getString(R.string.admob_interstitial_unit_id)
            mInterstitialAd?.loadAd(AdRequest.Builder().build())
            mInterstitialAd?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    LOGApp.e("Add Loaded")
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    LOGApp.e("Add Failed To Load: " + errorCode)
                    addClose.value = true
                }

                override fun onAdClosed() {
                    addClose.value = true
                    mInterstitialAd?.loadAd(AdRequest.Builder().build())
                }
            }
        }

        fun initAdMob() {
            if ((sharedPreference.userDetail?.isSubscripbed() == false) && (AppConfig.BANNER_AD || AppConfig.INTERSTITIAL_AD)) {

//            if (AppConfig.INTERSTITIAL_AD)
//                initInterstitialAdds()
//            MobileAds.initialize(this, getString(R.string.admob_app_id))
                MobileAds.initialize(this) {}
                if (AppConfig.INTERSTITIAL_AD && mInterstitialAd == null)
                    initInterstitialAdds()
            }
        }

        /**
         * Initialise Plaid sdk
         */
        private fun initPlaidSdk() {
            val plaidOptions = PlaidOptions(
                if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.ASSERT,
                PlaidEnvironment.SANDBOX
            )
//        Plaid.create(this, plaidOptions)
            Plaid.setOptions(plaidOptions)
        }

}
