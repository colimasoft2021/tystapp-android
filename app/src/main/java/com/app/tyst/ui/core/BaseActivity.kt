package com.app.tyst.ui.core

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.Settings
import com.app.tyst.data.model.response.VersionConfigResponse
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.ui.settings.staticpages.StaticPagesActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.checkInternetConnected
import com.app.tyst.utility.extension.hideKeyBoard
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.helper.LOGApp
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.hb.logger.Logger
import com.hb.logger.data.model.CustomLog
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.materialdesigniconic.MaterialDesignIconic

/**
 * Created by HB on 21/8/19.
 */
abstract class BaseActivity : AppCompatActivity() {

    private var progressDialogFragment: ProgressDialogFragment? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    /**
     * Handling all type of permission
     */
    lateinit var currentFragment: BaseFragment<*>

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).setCurrentActivity(this)
        progressDialogFragment = ProgressDialogFragment.newInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        addObserver()
    }

    override fun onResume() {
        super.onResume()
        logger.debugEvent("onResume called", "Activity resumed")
    }

    override fun onPause() {
        super.onPause()
        logger.debugEvent("onPause called", "Activity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.debugEvent("onDestroy called", "Activity destroyed")
    }

    private fun addObserver() {
        (application as MainApplication).versionLiveData.observe(this@BaseActivity, Observer {
            (application as MainApplication).versionLiveData
            if (it.settings?.isSuccess == true) {
                sharedPreference.maxTaxPercentage = it.data?.maximumTaxPerc
                sharedPreference.maxTipPercentage = it.data?.maximumTipPerc
                when {
                    it.data?.shouldShowVersionDialog(this@BaseActivity) == true -> {
                        showNewVersionAvailableDialog(it.data ?: VersionConfigResponse())
                    }
                    it.data?.shouldShowTNCUpdated() == true -> {
                        startActivity(StaticPagesActivity.getStartIntent(this@BaseActivity, IConstants.STATIC_PAGE_TERMS_CONDITION, getString(R.string.terms_n_conditions),forceUpdate = true,versionResponse =  it.data?:VersionConfigResponse()))
                    }
                    it.data?.shouldShowPrivacyPolicyUpdated() == true -> {
                        startActivity(StaticPagesActivity.getStartIntent(this@BaseActivity, IConstants.STATIC_PAGE_PRIVACY_POLICY, getString(R.string.privacy_policy),forceUpdate = true,versionResponse =  it.data?:VersionConfigResponse()))
                    }
                }
            } else if (!handleApiError(it.settings)) {
            }
        })
    }

    /* Hide soft keyboard when user click on outside of keyboard
    */
    fun setupUI(view: View?) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view?.setOnTouchListener { _, _ ->
                view.hideKeyBoard()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    /**
     * Navigate user to Login Screen if user not logged in or want to logout
     */
    fun navigateToLoginScreen(isLogOut: Boolean) {
        if (isLogOut) {
            sharedPreference.userDetail = null
            sharedPreference.isLogin = false
            sharedPreference.authToken = ""
            sharedPreference.setInstitutionsList(null)
        }
        val intent = Intent(this@BaseActivity, (application as MainApplication).getLoginActivity())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Navigate user to Home Screen after successfully login/signup
     */
    fun navigateToHomeScreen() {
        (application as MainApplication).initAdMob()
        val intent = Intent(this@BaseActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * If user login in another device, then show session expire dialog to user and navigate to login screen
     */
    fun showSessionExpireDialog() {
        DialogUtil.alert(context = this@BaseActivity,
            msg = getString(R.string.msg_looged_in_from_other_device),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    navigateToLoginScreen(true)
                }

                override fun onCancel(isNeutral: Boolean) {
                    navigateToLoginScreen(true)
                }
            },
            isCancelable = false)
    }

    fun handleApiError(settings: Settings?,
                       showError: Boolean = true,
                       showSessionExpire: Boolean = true): Boolean {
        showHideProgressDialog(false)
        return when (settings?.success) {
            Settings.AUTHENTICATION_ERROR -> {
                if (showSessionExpire) showSessionExpireDialog()
                true

            }
            Settings.NETWORK_ERROR -> {
                settings.message.showSnackBar(this@BaseActivity)
                true
            }
            "0" -> false
            else -> {
                if (showError) settings?.message?.showSnackBar(this@BaseActivity)
                true
            }
        }
    }


    fun showHideProgressDialog(isShow: Boolean, message: String = "") {
        try {
            if (isShow) {
                if (progressDialogFragment?.dialog == null || progressDialogFragment?.dialog?.isShowing == false) {
                    ProgressDialogFragment.message = message
                    progressDialogFragment?.show(
                        supportFragmentManager,
                        javaClass.simpleName)
                }
            } else {
                progressDialogFragment?.dismissAllowingStateLoss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isNetworkConnected(): Boolean {
        return if (checkInternetConnected(this@BaseActivity)) {
            true
        } else {
            getString(R.string.msg_check_internet_connection).showSnackBar(this@BaseActivity)
            logger.warningEvent("Network", getString(R.string.msg_check_internet_connection))
            false
        }
    }

    /**
     * This method will show banner ads
     * @param adView AdView
     */
    fun showAdMob(adView: AdView) {
        if ((sharedPreference.userDetail?.isSubscripbed() == true) || !AppConfig.BANNER_AD) {
            adView.visibility = View.GONE
        } else {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = adListener
            logger.debugEvent("Banner Ad Request", "Banner Ad Requested")
        }
    }

    /**
     * This method will show interstitial adds after every 4 event
     * @return Boolean
     */
    fun showInterstitial(): Boolean {
        // Show the ad if it is ready. Otherwise toast and reload the ad.
        return if ((sharedPreference.userDetail?.isSubscripbed() == true) || !AppConfig.INTERSTITIAL_AD)
            false else if (++(application as MainApplication).counterIntertitialAdd % 4 == 0) {
            LOGApp.e("Count:--" + (application as MainApplication).counterIntertitialAdd)
            if ((application as MainApplication).mInterstitialAd?.isLoaded == true) {
                (application as MainApplication).mInterstitialAd?.show()
                true
            } else {
                (application as MainApplication).mInterstitialAd?.loadAd(AdRequest.Builder().build())
                false
            }
        } else {
            false
        }

    }

    /**
     * Show dialog for new application version is available
     * @param version VersionConfigResponse
     */
    private fun showNewVersionAvailableDialog(version: VersionConfigResponse) {

        var dialog: MaterialStyledDialog? = null
        val icon = IconicsDrawable(this).icon(MaterialDesignIconic.Icon.gmi_google_play)
        icon.setTint(ContextCompat.getColor(this@BaseActivity, R.color.white))

        val builder = MaterialStyledDialog.Builder(this).apply {
            setIcon(icon)
            withDialogAnimation(true)
            setTitle(getString(R.string.new_version_available))
            setDescription(version.versionCheckMessage ?: "")
            autoDismiss(false)
            setScrollable(true)
            setPositiveText(R.string.update)
            if (version.isOptionalUpdate())
                setNegativeText(R.string.not_now)
            setCancelable(version.isOptionalUpdate())
            onPositive { _, _ ->
                val appPackageName = packageName // getPackageName() from Context or Activity object
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }

            onNegative { _, _ ->
                dialog?.dismiss()
            }
        }
        dialog = builder.show()
    }

    /**
     * Log all firebase events
     * @param id String Id of event
     * @param name String Name of event
     * @param contentType String content type
     */
    fun setFireBaseAnalyticsData(id: String, name: String, contentType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
//        logger.dumpCustomEvent(name, contentType)
    }

    private val adListener = object: AdListener() {
        override fun onAdLoaded() {
            logger.debugEvent("Banner Ad", "Add Loaded")
        }

        override fun onAdFailedToLoad(errorCode : Int) {
            logger.debugEvent("Banner Ad", "ERROR "+errorCode, status = CustomLog.STATUS_ERROR)
        }

        override fun onAdOpened() {
            logger.debugEvent("Banner Ad", "onAdOpened")
        }

        override fun onAdClicked() {
            logger.debugEvent("Banner Ad", "onAdClicked")
        }

        override fun onAdLeftApplication() {
            logger.debugEvent("Banner Ad", "onAdLeftApplication")
        }

        override fun onAdClosed() {
            logger.debugEvent("Banner Ad", "onAdClosed")
        }
    }

}
