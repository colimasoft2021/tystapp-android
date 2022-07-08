package com.app.tyst.ui.authentication.otp.otpforgotphonenumber

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.ActivityOtpBinding
import com.app.tyst.ui.authentication.resetpassword.ResetPasswordActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.validation.*

/**
 * This activity is used to verify user's phone number. A 4 digit verification code will send to user phone number and user
 * will enter received 4 digit pin to verify his/her phone number
 */
class OTPActivity : BaseActivity() {

    companion object {

        fun getStartIntent(context: Context, phoneNumber: String, otp: String, resetKey: String): Intent {
            return Intent(context, OTPActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
                putExtra("resetKey", resetKey)
            }
        }
    }

    lateinit var binding: ActivityOtpBinding
    private var phoneNumber = ""
    var otp = "" // User received OTP
    private var resetKey = ""

    private val viewModel: OTPViewModel
        get() = ViewModelProviders.of(this).get(OTPViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@OTPActivity, R.layout.activity_otp)
        initView()
    }

    private fun initView() {
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
            resetKey = getStringExtra("resetKey") ?: ""
        }

        binding.apply {
            lifecycleOwner = this@OTPActivity
            phoneNumber = this@OTPActivity.phoneNumber
            time = viewModel.getTimerValue()
            enableRetry = viewModel.getEnableRetrySetting()
            btnValidate.makeBounceable()
            btnRetry.makeBounceable()
            Toast.makeText(this@OTPActivity, otp, Toast.LENGTH_LONG).show()
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
                viewModel.callForgotPassword(PhoneNumberUtils.normalizeNumber(this@OTPActivity.phoneNumber))
            }

            btnValidate.clickWithDebounce {
                navigateToResetPassword()
            }
        }
    }

    private fun addObserver() {

        viewModel.validationObserver.observe(this@OTPActivity, Observer {
            when {
                it.failType == OTP_EMPTY -> {
                    getString(R.string.alert_enter_otp).showSnackBar(this@OTPActivity)
                }

                it.failType == OTP_INVALID -> {
                    getString(R.string.alert_invalid_otp).showSnackBar(this@OTPActivity)
                }
            }
        })

        viewModel.resendOtpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                it?.settings?.message?.showSnackBar(this@OTPActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                resetKey = it.data?.resetKey ?: ""
                otp = it.data?.otp ?: ""
                Toast.makeText(this@OTPActivity, otp, Toast.LENGTH_LONG).show()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@OTPActivity)
            }
            mIdlingResource?.setIdleState(true)
        })
    }

    private fun navigateToResetPassword() {
        if (viewModel.isValid(otp = binding.otpView.text.toString(), sendOtp = otp)) {
            viewModel.cancelTimer(markFinish = true)
            startActivity(ResetPasswordActivity.getStartIntent(this@OTPActivity, phoneNumber = PhoneNumberUtils.normalizeNumber(phoneNumber),
                    resetKey = resetKey))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelTimer()
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
