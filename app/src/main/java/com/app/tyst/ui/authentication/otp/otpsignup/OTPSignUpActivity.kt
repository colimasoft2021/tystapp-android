package com.app.tyst.ui.authentication.otp.otpsignup

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
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.databinding.ActivityOtpSignUpBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar

/**
 * This activity will open when user try to signup with phone number or social with phone number.
 * User need to verify his/her phone number before signup
 */
class OTPSignUpActivity : BaseActivity() {

    companion object {

        fun getStartIntent(context: Context, phoneNumber: String, otp: String, request: SignUpRequestModel): Intent {
            return Intent(context, OTPSignUpActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
                putExtra("request", request)
            }
        }
    }

    lateinit var binding: ActivityOtpSignUpBinding
    private var phoneNumber = ""
    var otp = "" // User received OTP
    private var request: SignUpRequestModel? = null
    private val viewModel: OTPSignUpViewModel
        get() = ViewModelProviders.of(this).get(OTPSignUpViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@OTPSignUpActivity, R.layout.activity_otp_sign_up)
        initView()
    }

    private fun initView() {
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
            request = getParcelableExtra("request")
        }

        binding.apply {
            lifecycleOwner = this@OTPSignUpActivity
            phoneNumber = this@OTPSignUpActivity.phoneNumber
            time = viewModel.getTimerValue()
            enableRetry = viewModel.getEnableRetrySetting()
            btnValidate.makeBounceable()
            btnRetry.makeBounceable()
            Toast.makeText(this@OTPSignUpActivity, otp, Toast.LENGTH_LONG).show()
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
                mIdlingResource?.setIdleState(false)
                viewModel.startTimer()
                showHideProgressDialog(true)
                viewModel.callResendOtp(PhoneNumberUtils.normalizeNumber(this@OTPSignUpActivity.phoneNumber))
            }

            btnValidate.clickWithDebounce {
                callSignUp()
            }
        }
    }

    private fun addObserver() {
        viewModel.settingObserver.observe(this@OTPSignUpActivity, Observer {
            showHideProgressDialog(false)
            if (it?.isSuccess == false) {
                it.message.showSnackBar(this@OTPSignUpActivity)
            }
        })

        viewModel.otpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                otp = it.data?.get(0)?.otp ?: ""
                Toast.makeText(this@OTPSignUpActivity, otp, Toast.LENGTH_LONG).show()
                it.settings?.message?.showSnackBar(this@OTPSignUpActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@OTPSignUpActivity)
            }
            mIdlingResource?.setIdleState(true)
        })

        viewModel.signUpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                if (it?.data == null) {
                    it.settings?.message?.showSnackBar(this@OTPSignUpActivity,
                            IConstants.SNAKBAR_TYPE_SUCCESS,
                            duration = IConstants.SNAKE_BAR_SHOW_TIME)
                    Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
                } else {
                    viewModel.saveUserDetails(it?.data?.getUserDetails?.get(0))
                    navigateToHomeScreen()
                }
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@OTPSignUpActivity)
            }
            mIdlingResource?.setIdleState(true)
        })
    }

    private fun callSignUp() {
        if (viewModel.isValid(otp = binding.otpView.text.toString(), sendOtp = otp)) {
            request?.apply {
                showHideProgressDialog(true)
                mIdlingResource?.setIdleState(false)
                viewModel.callSignUp(request = this)
            }
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
