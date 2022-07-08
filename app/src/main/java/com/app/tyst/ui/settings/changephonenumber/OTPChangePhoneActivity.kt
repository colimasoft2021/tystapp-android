package com.app.tyst.ui.settings.changephonenumber

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.ActivityOtpchangePhoneBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.validation.OTP_EMPTY
import com.app.tyst.utility.validation.OTP_INVALID

/**
 * This activity is used to verify OTP for Change Phone Number. A 4 digit verification code will send to user's new phone number and user
 * will enter received 4 digit pin to change his/her phone number
 */
class OTPChangePhoneActivity : BaseActivity() {

    companion object {

        fun getStartIntent(context: Context, phoneNumber: String, otp: String): Intent {
            return Intent(context, OTPChangePhoneActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
            }
        }
    }

    lateinit var binding: ActivityOtpchangePhoneBinding
    private var phoneNumber = ""
     var otp = "" // User received OTP

    private val viewModel: OTPChangePhoneViewModel
        get() = ViewModelProviders.of(this).get(OTPChangePhoneViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@OTPChangePhoneActivity, R.layout.activity_otpchange_phone)
        initView()
    }

    private fun initView() {
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
        }

        binding.apply {
            lifecycleOwner = this@OTPChangePhoneActivity
            phoneNumber = this@OTPChangePhoneActivity.phoneNumber
            time = viewModel.getTimerValue()
            enableRetry = viewModel.getEnableRetrySetting()
            btnValidate.makeBounceable()
            btnRetry.makeBounceable()
            Toast.makeText(this@OTPChangePhoneActivity, otp, Toast.LENGTH_LONG).show()
        }

        initListeners()
        addObserver()
        viewModel.startTimer()
    }

    private fun initListeners() {
        binding.apply {

            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }

            btnRetry.clickWithDebounce {
                viewModel.startTimer()
                showHideProgressDialog(true)
                mIdlingResource?.setIdleState(false)
                viewModel.callResendOTP(type = "phone", email = "", userName = "", phone = PhoneNumberUtils.normalizeNumber(this@OTPChangePhoneActivity.phoneNumber))
            }

            btnValidate.clickWithDebounce {
                validateOTP()
            }
        }
    }

    private fun addObserver() {

        viewModel.validationObserver.observe(this@OTPChangePhoneActivity, Observer {
            when {
                it.failType == OTP_EMPTY -> {
                    getString(R.string.alert_enter_otp).showSnackBar(this@OTPChangePhoneActivity)
                }

                it.failType == OTP_INVALID -> {
                    getString(R.string.alert_invalid_otp).showSnackBar(this@OTPChangePhoneActivity)
                }
            }
        })

        viewModel.otpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                mIdlingResource?.setIdleState(true)
                it?.settings?.message?.showSnackBar(this@OTPChangePhoneActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                otp = it.data?.get(0)?.otp ?: ""
                Toast.makeText(this@OTPChangePhoneActivity, otp, Toast.LENGTH_LONG).show()
            } else if (!handleApiError(it.settings)) {
                mIdlingResource?.setIdleState(true)
                it?.settings?.message?.showSnackBar(this@OTPChangePhoneActivity)
            }
        })

        viewModel.changePhoneNumberData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.settings?.isSuccess == true) {
                mIdlingResource?.setIdleState(true)
                viewModel.updatePhoneNumber(PhoneNumberUtils.normalizeNumber(this@OTPChangePhoneActivity.phoneNumber))
                it.settings?.message?.showSnackBar(this@OTPChangePhoneActivity, IConstants.SNAKBAR_TYPE_SUCCESS, duration = IConstants.SNAKE_BAR_SHOW_TIME)
                Handler().postDelayed({ navigateToSettingScreen() }, IConstants.SNAKE_BAR_SHOW_TIME)

            } else {
                mIdlingResource?.setIdleState(true)
                it.settings?.message?.showSnackBar(this@OTPChangePhoneActivity)
            }
        })
    }

    /**
     * Verify entered OTP with received OTP
     */
    private fun validateOTP() {
        if (viewModel.isValid(otp = binding.otpView.text.toString(), sendOtp = otp)) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callChangePhoneNumber(PhoneNumberUtils.normalizeNumber(this@OTPChangePhoneActivity.phoneNumber))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelTimer()
    }

    private fun navigateToSettingScreen() {
        val intent = Intent(this@OTPChangePhoneActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null
    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }
        return mIdlingResource as RemoteIdlingResource
    }
}
