package com.app.tyst

//import com.crashlytics.android.Crashlytics
//import com.plaid.linkbase.models.PlaidEnvironment
//import com.plaid.linkbase.models.PlaidOptions
//import com.plaid.plog.LogLevel

import android.R
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hb.logger.Logger.Companion.initializeSession
import com.hb.logger.Logger.Companion.setExceptionCallbackListener
import com.plaid.link.Plaid
import com.plaid.linkbase.models.configuration.PlaidEnvironment
import com.plaid.linkbase.models.configuration.PlaidOptions
import com.plaid.log.LogLevel
import netscape.javascript.JSObject.getWindow
import java.lang.ref.WeakReference
import java.util.logging.Logger


/**
 * Created by HB on 21/8/19.
 */
class MainApplication : Application() {
    var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
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


        MobileAds.initialize(this) {}
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
/*        private fun initInterstitialAdds() {
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
        }*/

        fun initAdMob() {
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                        addClose.value = true
                        loadAd()
                    }
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        Log.d(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }

                    override fun onAdImpression() {
                        //usare este metodo para que regrese true si se mostro e
                    }
                }
                mInterstitialAd?.show(this.getCurrentActivity()!!)
            } else {
                Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
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

     fun loadAd(){
        val adRequest = AdRequest.Builder().build()

        //cargando el servicio de anuncios de google
        InterstitialAd.load(this,"ca-app-pub-5963823178389539~1792060567", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
                addClose.value = true
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }


        })


    }



}
