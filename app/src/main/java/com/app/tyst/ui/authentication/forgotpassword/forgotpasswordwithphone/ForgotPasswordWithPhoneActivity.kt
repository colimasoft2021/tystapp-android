package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone

import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.ActivityForgotPasswordWithPhoneBinding
import com.app.tyst.ui.authentication.otp.otpforgotphonenumber.OTPActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID

class ForgotPasswordWithPhoneActivity : BaseActivity() {
    companion object {

        const val TAG = "ForgotPasswordWithPhoneActivityTest"
    }
    lateinit var binding: ActivityForgotPasswordWithPhoneBinding
    private val viewModel: ForgotPasswordPhoneViewModel
        get() = ViewModelProviders.of(this).get(ForgotPasswordPhoneViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ForgotPasswordWithPhoneActivity,
                R.layout.activity_forgot_password_with_phone)
        initView()
    }

    private fun initView() {
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        initListeners()
        addObserver()
    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@ForgotPasswordWithPhoneActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == PHONE_NUMBER_EMPTY -> {
                    getString(R.string.alert_enter_mobile_number).showSnackBar(this@ForgotPasswordWithPhoneActivity)
                }

                it.failType == PHONE_NUMBER_INVALID -> {
                    getString(R.string.alert_invalid_phone_number).showSnackBar(this@ForgotPasswordWithPhoneActivity)
                }
            }
        })

        viewModel.forgotPasswordLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                it.settings!!.message.showSnackBar(this@ForgotPasswordWithPhoneActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS,
                        duration = IConstants.SNAKE_BAR_SHOW_TIME)
                mIdlingResource?.setIdleState(true)
                Handler().postDelayed({
                    startActivity(OTPActivity.getStartIntent(context = this@ForgotPasswordWithPhoneActivity,
                            phoneNumber = binding.inputPhoneNumber.getTrimText(),
                            otp = it.data?.otp ?: "",
                            resetKey = it?.data?.resetKey ?: ""))
                }, IConstants.SNAKE_BAR_SHOW_TIME)

            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@ForgotPasswordWithPhoneActivity)
                mIdlingResource?.setIdleState(true)
            }else{
                mIdlingResource?.setIdleState(true)
            }

        })
    }

    private fun initListeners() {
        binding.btnBack.makeBounceable()
        binding.btnBack.clickWithDebounce {
            binding.root.hideKeyBoard()
            finish()
        }
        binding.btnSend.clickWithDebounce {
            binding.root.hideKeyBoard()
            if (isNetworkConnected()) {
                sendResetLink(PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()))
            }}
        binding.inputPhoneNumber.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            binding.inputPhoneNumber.hideKeyBoard()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendResetLink(PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()))
                return@OnEditorActionListener true
            }
            false
        })
    }

    /**
     * Api call for send OTP to user phone number
     * @param phone String User;s phone number on which otp will received
     */
    private fun sendResetLink(phone: String) {
        if (viewModel.isValid(phone)) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callForgotPassword(phone)
        }
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
