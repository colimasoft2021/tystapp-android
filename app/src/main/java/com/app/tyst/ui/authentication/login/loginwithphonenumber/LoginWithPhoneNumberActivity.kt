package com.app.tyst.ui.authentication.login.loginwithphonenumber

import android.content.Intent
import android.os.Bundle
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
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.databinding.ActivityLoginWithPhoneNumberBinding
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone.ForgotPasswordWithPhoneActivity
import com.app.tyst.ui.authentication.register.SignUpActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.*

class LoginWithPhoneNumberActivity : BaseActivity() {

    lateinit var binding: ActivityLoginWithPhoneNumberBinding
    private val viewModel: LoginWithPhoneNumberViewModel
        get() = ViewModelProviders.of(this).get(LoginWithPhoneNumberViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_PHONE)
        binding = DataBindingUtil.setContentView(this@LoginWithPhoneNumberActivity, R.layout.activity_login_with_phone_number)
        initView()
    }

    private fun initView() {
        binding.apply {
            btnForgotPassword.makeBounceable()
            btnSkip.makeBounceable()
            btnSignUp.makeBounceable()
            btnLogin.makeBounceable()
        }
        addObserver()
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        initListener()

//        fillDummyLogin()
    }

    private fun initListener() {
        binding.apply {
            btnLogin.clickWithDebounce {
                binding.root.hideKeyBoard()
                if (isNetworkConnected())
                    login()
            }

            btnSkip.clickWithDebounce {
                sharedPreference.isSkip = true
                navigateToHomeScreen()
            }

            btnSignUp.clickWithDebounce {
                binding.root.hideKeyBoard()
                startActivity(Intent(this@LoginWithPhoneNumberActivity, SignUpActivity::class.java))
            }

            btnForgotPassword.clickWithDebounce {
                binding.root.hideKeyBoard()
                startActivity(Intent(this@LoginWithPhoneNumberActivity,
                        ForgotPasswordWithPhoneActivity::class.java))
            }

            inputPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                inputPassword.hideKeyBoard()
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login()
                    return@OnEditorActionListener true
                }
                false
            })
        }
    }

    private fun addObserver() {

        viewModel.validationObserver.observe(this@LoginWithPhoneNumberActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == PHONE_NUMBER_EMPTY -> {
                    getString(R.string.alert_enter_mobile_number).showSnackBar(this@LoginWithPhoneNumberActivity)
                }

                it.failType == PHONE_NUMBER_INVALID -> {
                    getString(R.string.alert_invalid_phone_number).showSnackBar(this@LoginWithPhoneNumberActivity)
                }
                it.failType == PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_password).showSnackBar(this@LoginWithPhoneNumberActivity)
                }
            }
        })

        viewModel.loginResponseMutableLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                viewModel.saveUserDetails(it?.data)
                mIdlingResource?.setIdleState(true)
                navigateToHomeScreen()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@LoginWithPhoneNumberActivity)
                mIdlingResource?.setIdleState(true)
            }
        })

    }

    /**
     * Perform login
     */
    private fun login() {

        if (viewModel.isValid(phoneNumber = PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()),
                        password = binding.inputPassword.getTrimText())) {
            binding.inputPassword.hideKeyBoard()
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callLogin(phoneNumber = PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()),
                    password = binding.inputPassword.getTrimText())
        }
    }

    private fun fillDummyLogin() {
        binding.inputPhoneNumber.setText("9490037755")
        binding.inputPassword.setText("Abc@123")
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
