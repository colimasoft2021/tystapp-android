package com.app.tyst.ui.authentication.login.loginwithemailsocial

import android.app.Activity
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
import com.app.tyst.data.model.Social
import com.app.tyst.databinding.ActivityLoginWithEmailSocialBinding
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail.ForgotPasswordWithEmailActivity
import com.app.tyst.ui.authentication.register.SignUpActivity
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.social.FacebookLoginManager
import com.app.tyst.ui.social.GoogleLoginManager
import com.app.tyst.ui.tutorial.TutorialActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.PASSWORD_EMPTY
import com.plaid.link.Plaid
import com.plaid.linkbase.models.connection.LinkCancellation
import com.plaid.linkbase.models.connection.LinkConnection
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler

//import com.plaid.linkbase.models.LinkCancellation
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.LinkEventListener
//import com.plaid.linkbase.models.PlaidApiError

/**
 * This is login activity for login with email and password OR Social.
 */
class LoginWithEmailSocialActivity : BaseActivity() {

    lateinit var binding: ActivityLoginWithEmailSocialBinding
    private lateinit var social: Social

    private val viewModel: LoginWithEmailSocialViewModel
        get() = ViewModelProviders.of(this).get(LoginWithEmailSocialViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_EMAIL_SOCIAL)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_with_email_social)
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
        initListener()
//        fillDummyLogin()
    }

    private fun initListener() {

        binding.apply {
            btnLogin.clickWithDebounce {
                binding.root.hideKeyBoard()
//                openPlaidSdk()
                if (isNetworkConnected())
                    login()
            }

            btnForgotPassword.clickWithDebounce { }

            btnFb.clickWithDebounce {
                if (isNetworkConnected()) {
                    facebookLogin()
                }
            }

            btnGoogle.clickWithDebounce {
                if (isNetworkConnected()) {
                    googleLogin()
                }
            }

            btnSkip.clickWithDebounce {
                sharedPreference.isSkip = true
                navigateToHomeScreen()
            }

            btnSignUp.clickWithDebounce {
                if (isNetworkConnected()) {
                    startActivity(Intent(this@LoginWithEmailSocialActivity, SignUpActivity::class.java))
                }
            }

            btnForgotPassword.clickWithDebounce {
                if (isNetworkConnected()) {
                    startActivity(Intent(this@LoginWithEmailSocialActivity,
                            ForgotPasswordWithEmailActivity::class.java))
                }
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
        viewModel.validationObserver.observe(this@LoginWithEmailSocialActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when (it.failType) {
                EMAIL_EMPTY -> {
                    getString(R.string.alert_enter_email).showSnackBar(this@LoginWithEmailSocialActivity)
                }
                EMAIL_INVALID -> {
                    getString(R.string.alert_enter_valid_email).showSnackBar(this@LoginWithEmailSocialActivity)
                }
                PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_password).showSnackBar(this@LoginWithEmailSocialActivity)
                }
            }
        })

        viewModel.loginResponseMutableLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.success == "1") {
                viewModel.saveUserDetails(it?.data?.getUserDetails?.get(0))
//                if (it.data?.getInstitutions.isNullOrEmpty()) {
//                    openPlaidSdk()
//                } else {
                viewModel.saveInstitutionDetail(it.data?.getInstitutions)
                mIdlingResource?.setIdleState(true)
                if (sharedPreference.isTutorialShowed) {
                    navigateToHomeScreen()
                } else {
                    startActivity(TutorialActivity.getStartIntent(this@LoginWithEmailSocialActivity, false))
                    finish()
                }
//                }
            } else if (it.settings?.success == "3") {
                /*  viewModel.saveUserDetails(it?.data,false)
                  it.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity)*/
                showResendEmailDialog(it?.settings?.message ?: "")
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.addAccountLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                viewModel.saveInstitutionDetail(it.data)
                mIdlingResource?.setIdleState(true)
                navigateToHomeScreen()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity)
                viewModel.clearUserLoginData()
                mIdlingResource?.setIdleState(true)
            } else {
                viewModel.clearUserLoginData()
                mIdlingResource?.setIdleState(true)
            }
        })


        viewModel.socialLoginMutableLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.success == "1") {
                viewModel.saveUserDetails(it?.data?.getUserDetails?.get(0))
//                if (it.data?.getInstitutions.isNullOrEmpty()) {
//                    openPlaidSdk()
//                } else {
                viewModel.saveInstitutionDetail(it.data?.getInstitutions)
                mIdlingResource?.setIdleState(true)
                if (sharedPreference.isTutorialShowed) {
                    navigateToHomeScreen()
                } else {
                    startActivity(TutorialActivity.getStartIntent(this@LoginWithEmailSocialActivity, false))
                    finish()
                }
//                }
            } else if (it.settings?.success == "2") {
                startActivity(SignUpActivity.getStartIntent(mContext = this@LoginWithEmailSocialActivity, social = social))
                mIdlingResource?.setIdleState(true)
            } else if (!handleApiError(it.settings)) {
                viewModel.clearUserLoginData()
                it?.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                viewModel.clearUserLoginData()
                mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.sendLinkData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                it?.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                mIdlingResource?.setIdleState(true)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@LoginWithEmailSocialActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }
        })

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
//            binding.inputPassword.getTrimText().toEncrypt().toDecrypt()
            viewModel.callLogin(email = binding.inputEmail.getTrimText(),
                    password = binding.inputPassword.getTrimText(), isEncrypted = (application as MainApplication).isEncryptionApply)
        }
    }

    /**
     * Perform Google Login
     */
    private fun googleLogin() {
        binding.root.hideKeyBoard()
        mIdlingResource?.setIdleState(false)
        val intent = Intent(this@LoginWithEmailSocialActivity, GoogleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_GOOGLE_LOGIN)
    }

    /**
     * Perform Facebook Login
     */
    private fun facebookLogin() {
        binding.root.hideKeyBoard()
        mIdlingResource?.setIdleState(false)
        val intent = Intent(this@LoginWithEmailSocialActivity, FacebookLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_FACEBOOK_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == IConstants.REQUEST_PLAID_LINK_CODE ->
            { if (!myPlaidLinkActivityResultHandler.onActivityResult(requestCode, resultCode, data)) {
                logger.debugEvent("Plaid Response", "Not handled")
            }}
            resultCode == Activity.RESULT_OK -> when (requestCode) {
                IConstants.REQUEST_CODE_FACEBOOK_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("facebook_data")
                    if (mSocial != null) {
                        social = mSocial
                        showHideProgressDialog(true)
                        viewModel.callLogin(socialType = IConstants.SOCIAL_TYPE_FB,
                                socialId = social.socialId ?: "")
                    } else {
                        getString(R.string.msg_no_user_data).showSnackBar(this@LoginWithEmailSocialActivity)
                        mIdlingResource?.setIdleState(true)
                    }
                }

                IConstants.REQUEST_CODE_GOOGLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("google_data")
                    if (mSocial != null) {
                        social = mSocial
                        showHideProgressDialog(true)
                        viewModel.callLogin(socialType = IConstants.SOCIAL_TYPE_GOOGLE,
                                socialId = social.socialId ?: "")
                    } else {
                        getString(R.string.msg_no_user_data).showSnackBar(this@LoginWithEmailSocialActivity)
                        mIdlingResource?.setIdleState(true)
                    }
                }
            }
            else -> mIdlingResource?.setIdleState(true)
        }
    }

    private fun openPlaidSdk() {
        Plaid.setLinkEventListener(linkEventListener = {
            LOGApp.e("Event", it.toString())
        })
        Plaid.openLink(
                this,
                viewModel.getLinkConfiguration(),
                IConstants.REQUEST_PLAID_LINK_CODE
        )
    }

    private val myPlaidLinkActivityResultHandler by lazy {
        PlaidLinkResultHandler(
                requestCode = IConstants.REQUEST_PLAID_LINK_CODE,
                onSuccess = {
                    var accountIds = ""
                    var accountNames = ""
                    var accountNumbers = ""
                    it.linkConnectionMetadata.accounts.forEachIndexed { index, linkAccount ->
                        if (index == 0) {
                            accountIds = linkAccount.accountId
                            accountNames = linkAccount.accountName ?: ""
                            accountNumbers = linkAccount.accountNumber ?: ""
                        } else {
                            accountIds += ", " + linkAccount.accountId
                            accountNames += ", " + linkAccount.accountName ?: ""
                            accountNumbers += ", " + linkAccount.accountNumber ?: ""
                        }

                    }

                    var info = "##Public Token: "+it.publicToken+"                   ##Account Ids:"+accountIds+"         ##Account Names: "+accountNames+"                   ##Account Numbers: "+accountNumbers+"          ##Institution Id: "+it.linkConnectionMetadata.institutionId+"         ##Institution Name: "+it.linkConnectionMetadata.institutionName
                    logger.debugEvent("Plaid Success", info)
                    if (isNetworkConnected()) {
                        showHideProgressDialog(true)
                        viewModel.callAddBankAccount(it)
                    } else {
                        mIdlingResource?.setIdleState(true)
                    }
                },
                onCancelled = {
                    viewModel.clearUserLoginData()
                    logger.debugEvent("Plaid Cancelled", getString(
                            R.string.content_cancelled,
                            it.institutionId,
                            it.institutionName,
                            it.linkSessionId,
                            it.status
                    ))
                    it.institutionName ?: "" + " Failed after " +
                    it.status?.showSnackBar(this@LoginWithEmailSocialActivity)
                    mIdlingResource?.setIdleState(true)
                },
                onExit = {
                    viewModel.clearUserLoginData()
                    logger.debugEvent("Plaid Exit", getString(
                            R.string.content_exit,
                            it.displayMessage,
                            it.errorCode,
                            it.errorMessage,
                            it.linkExitMetadata.institutionId,
                            it.linkExitMetadata.institutionName,
                            it.linkExitMetadata.status
                    ))
                    it.displayMessage.showSnackBar(this@LoginWithEmailSocialActivity)
                    mIdlingResource?.setIdleState(true)
                }
        )
    }


    /**
     * Show email not verified dialog
     */
    private fun showResendEmailDialog(message: String) {
        mIdlingResource?.setIdleState(false)
        DialogUtil.alert(context = this@LoginWithEmailSocialActivity, msg = message + "\n" + getString(R.string.msg_resend_email),
                positiveBtnText = getString(R.string.resend), negativeBtnText = getString(R.string.cancel),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        showHideProgressDialog(true)
                        viewModel.callResendLink(email = binding.inputEmail.getTrimText())
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        mIdlingResource?.setIdleState(true)
                    }
                }, isCancelable = false)
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
