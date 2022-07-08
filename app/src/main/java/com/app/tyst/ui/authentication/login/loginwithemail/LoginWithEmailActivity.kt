package com.app.tyst.ui.authentication.login.loginwithemail

import android.content.Intent
import android.os.Bundle
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
import com.app.tyst.databinding.ActivityLoginWithEmailBinding
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail.ForgotPasswordWithEmailActivity
import com.app.tyst.ui.authentication.register.SignUpActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.PASSWORD_EMPTY


/**
 * This is login activity for login with email and password.
 */
class LoginWithEmailActivity : BaseActivity() {

    lateinit var binding: ActivityLoginWithEmailBinding
    private val viewModel: LoginWithEmailViewModel
        get() = ViewModelProviders.of(this).get(LoginWithEmailViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_EMAIL)
        binding = DataBindingUtil.setContentView(this@LoginWithEmailActivity, R.layout.activity_login_with_email)
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

            btnForgotPassword.clickWithDebounce { }

            btnSkip.clickWithDebounce {
                sharedPreference.isSkip = true
                navigateToHomeScreen()
            }

            btnSignUp.clickWithDebounce {
                startActivity(Intent(this@LoginWithEmailActivity, SignUpActivity::class.java))
            }

            btnForgotPassword.clickWithDebounce {
                startActivity(Intent(this@LoginWithEmailActivity,
                        ForgotPasswordWithEmailActivity::class.java))
            }

            inputPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                binding.inputPassword.hideKeyBoard()
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login()
                    return@OnEditorActionListener true
                }
                false
            })
        }

    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@LoginWithEmailActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == EMAIL_EMPTY -> {
                    getString(R.string.alert_enter_email).showSnackBar(this@LoginWithEmailActivity)
                }

                it.failType == EMAIL_INVALID -> {
                    getString(R.string.alert_enter_valid_email).showSnackBar(this@LoginWithEmailActivity)
                }
                it.failType == PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_password).showSnackBar(this@LoginWithEmailActivity)
                }
            }
        })

//        viewModel.loginResponseMutableLiveData.observe(this, Observer {
//            showHideProgressDialog(false)
//            if (it.settings?.isSuccess == true) {
//                viewModel.saveUserDetails(it?.data)
//                mIdlingResource?.setIdleState(true)
//                navigateToHomeScreen()
//            } else if (!handleApiError(it.settings)) {
//                it?.settings?.message?.showSnackBar(this@LoginWithEmailActivity)
//                mIdlingResource?.setIdleState(true)
//            }
//        })
    }

    /**
     * Perform Login
     */
    private fun login() {

        if (viewModel.isValid(email = binding.inputEmail.getTrimText(),
                        password = binding.inputPassword.getTrimText())) {

            binding.inputPassword.hideKeyBoard()
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callLogin(email = binding.inputEmail.getTrimText(),
                    password = binding.inputPassword.getTrimText())
        }
    }

    private fun fillDummyLogin() {
        binding.inputEmail.setText("priyanka.chillakuru@hiddenbrains.in")
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
