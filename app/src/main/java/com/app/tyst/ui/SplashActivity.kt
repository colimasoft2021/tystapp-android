package com.app.tyst.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import androidx.lifecycle.Observer
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.ui.settings.inappbilling.BillingRepository
import com.app.tyst.ui.tutorial.TutorialActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.helper.LOGApp
import com.crashlytics.android.Crashlytics
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LOGApp.e("Splash Created")
        setContentView(R.layout.activity_splash)
//        checkSubscriptionStatus()
    }

    //==== Splash screen time logic =====
    private val mDelayRunnable = {
        startActivity(Intent(this@SplashActivity, getLaunchClass()))
        overridePendingTransition(0, 0) //no animation
        this.finish()
    }

    private val mHandler = Handler()

    private fun delayed() {
        mHandler.removeCallbacks(mDelayRunnable)
        mHandler.postDelayed(mDelayRunnable, IConstants.SPLASH_TIME)
    }

    override fun onResume() {
        super.onResume()
        LOGApp.e("Splash Resume")
        delayed()
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mDelayRunnable)
    }

    /**
     * Choose activity to open
     *
     * @return if user already login, then open home activity, else open login activity
     */
    private fun getLaunchClass(): Class<*> {
//        sharedPreference.isLogin = true
//        sharedPreference.userDetail = LoginResponse()
//        sharedPreference.authToken = "99dfc4311e6d611a782301e045d6946f748c1acbe2ad87dd7c57e26f112ed7b6"
        return if (sharedPreference.isLogin) {
            // Return Home activity
            HomeActivity::class.java
        } else {
            //Return Login Activity
            (application as MainApplication).getLoginActivity()
        }
    }

    fun getKeyHash() {

        val info: PackageInfo
        try {
            info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                //String something = new String(Base64.encodeBytes(md.digest()));
                LOGApp.e("hash key", something)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            LOGApp.e("name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            LOGApp.e("no such an algorithm", e.toString())
        } catch (e: Exception) {
            LOGApp.e("exception", e.toString())
        }

    }

    private fun checkSubscriptionStatus() {
        val repository = BillingRepository.getInstance(application)
        repository.startDataSourceConnections()
        repository.subscriptionExpired.observe(this@SplashActivity, Observer {
            if (it) {
                val user = sharedPreference.userDetail
                user?.purchaseStatus = "No"
                sharedPreference.userDetail = user
            } else {
                val user = sharedPreference.userDetail
                user?.purchaseStatus = "Yes"
                sharedPreference.userDetail = user
            }
        })
    }
}
