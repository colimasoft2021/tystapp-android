package com.app.tyst.ui.authentication.login.loginwithphonenumbersocial

import android.app.Activity
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
import com.app.tyst.data.model.Social
import com.app.tyst.databinding.ActivityLoginWithPhoneNumberSocialBinding
import com.app.tyst.ui.social.FacebookLoginManager
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone.ForgotPasswordWithPhoneActivity
import com.app.tyst.ui.social.GoogleLoginManager
import com.app.tyst.ui.authentication.register.SignUpActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.PASSWORD_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID

class LoginWithPhoneNumberSocialActivity : BaseActivity() {

    lateinit var binding: ActivityLoginWithPhoneNumberSocialBinding
    private lateinit var social: Social
    private val viewModel: LoginWithPhoneNumberSocialViewModel
        get() = ViewModelProviders.of(this).get(LoginWithPhoneNumberSocialViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_PHONE_SOCIAL)
        binding = DataBindingUtil.setContentView(this@LoginWithPhoneNumberSocialActivity,
                R.layout.activity_login_with_phone_number_social)
        initView()
    }

    private fun initView() {
        binding.apply {
            btnForgotPassword.makeBounceable()
            btnSkip.makeBounceable()
            btnSignUp.makeBounceable()
            btnLogin.makeBounceable()
            btnFb.makeBounceable()
            btnGoogle.makeBounceable()
        }
        addObserver()
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        initListener()
//        fillDummyLogin()
    }

    private fun initListener() {
        binding.apply {
            btnLogin.clickWithDebounce {
                login()
            }

            btnForgotPassword.clickWithDebounce { }

            btnFb.clickWithDebounce { facebookLogin() }

            btnGoogle.clickWithDebounce { googleLogin() }

            btnSkip.clickWithDebounce {
                sharedPreference.isSkip = true
                navigateToHomeScreen()
            }

            btnSignUp.clickWithDebounce {
                startActivity(Intent(this@LoginWithPhoneNumberSocialActivity, SignUpActivity::class.java))
            }

            btnForgotPassword.clickWithDebounce {
                startActivity(Intent(this@LoginWithPhoneNumberSocialActivity,
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
        viewModel.validationObserver.observe(this@LoginWithPhoneNumberSocialActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == PHONE_NUMBER_EMPTY -> {
                    getString(R.string.alert_enter_mobile_number).showSnackBar(this@LoginWithPhoneNumberSocialActivity)
                }

                it.failType == PHONE_NUMBER_INVALID -> {
                    getString(R.string.alert_invalid_phone_number).showSnackBar(this@LoginWithPhoneNumberSocialActivity)
                }
                it.failType == PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_password).showSnackBar(this@LoginWithPhoneNumberSocialActivity)
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
                it?.settings?.message?.showSnackBar(this@LoginWithPhoneNumberSocialActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }
        })

//        viewModel.socialLoginMutableLiveData.observe(this, Observer {
//            showHideProgressDialog(false)
//            if (it.settings?.success == "1") {
//                viewModel.saveUserDetails(it?.data)
//                mIdlingResource?.setIdleState(true)
//                navigateToHomeScreen()
//            } else if (it.settings?.success == "2") {
//                startActivity(SignUpActivity.getStartIntent(mContext = this@LoginWithPhoneNumberSocialActivity, social = social))
//                mIdlingResource?.setIdleState(true)
//            } else if (!handleApiError(it.settings)) {
//                it?.settings?.message?.showSnackBar(this@LoginWithPhoneNumberSocialActivity)
//                mIdlingResource?.setIdleState(true)
//            } else {
//                mIdlingResource?.setIdleState(true)
//            }
//        })

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

    /**
     * Perform Google Login
     */
    private fun googleLogin() {
        binding.root.hideKeyBoard()
        mIdlingResource?.setIdleState(false)
        val intent = Intent(this@LoginWithPhoneNumberSocialActivity, GoogleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_GOOGLE_LOGIN)
    }

    /**
     * Perform Facebook Login
     */
    private fun facebookLogin() {
        binding.root.hideKeyBoard()
        mIdlingResource?.setIdleState(false)
        val intent = Intent(this@LoginWithPhoneNumberSocialActivity, FacebookLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_FACEBOOK_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IConstants.REQUEST_CODE_FACEBOOK_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("facebook_data")
                    if (mSocial != null) {
                        social = mSocial
                        showHideProgressDialog(true)
                        viewModel.callLogin(phoneNumber = "",
                                password = "",
                                socialType = IConstants.SOCIAL_TYPE_FB,
                                socialId = social.socialId ?: "")
                    } else {
                        getString(R.string.msg_no_user_data).showSnackBar(this@LoginWithPhoneNumberSocialActivity)
                        mIdlingResource?.setIdleState(true)
                    }
                }

                IConstants.REQUEST_CODE_GOOGLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("google_data")
                    if (mSocial != null) {
                        social = mSocial
                        showHideProgressDialog(true)
                        viewModel.callLogin(phoneNumber = "",
                                password = "",
                                socialType = IConstants.SOCIAL_TYPE_GOOGLE,
                                socialId = social.socialId ?: "")
                    } else {
                        getString(R.string.msg_no_user_data).showSnackBar(this@LoginWithPhoneNumberSocialActivity)
                        mIdlingResource?.setIdleState(true)
                    }
                }
            }
        } else {
            mIdlingResource?.setIdleState(true)
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
