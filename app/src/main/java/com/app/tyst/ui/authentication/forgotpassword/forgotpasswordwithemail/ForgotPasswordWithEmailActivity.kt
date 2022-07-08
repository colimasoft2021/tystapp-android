package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail

import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.ActivityForgotPasswordWithEmailBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.EMAIL_LENGTH

class ForgotPasswordWithEmailActivity : BaseActivity() {

    lateinit var binding: ActivityForgotPasswordWithEmailBinding
    private val viewModel: ForgotPasswordEmailViewModel
        get() = ViewModelProviders.of(this).get(ForgotPasswordEmailViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ForgotPasswordWithEmailActivity,
                R.layout.activity_forgot_password_with_email)
        initView()
    }

    companion object {
        const val TAG = "ForgotPasswordWithEmailActivityTest"
    }

    private fun initView() {
        initListeners()
        addObserver()
    }

    private fun initListeners() {
        binding.btnBack.makeBounceable()
        binding.btnBack.clickWithDebounce {
            binding.root.hideKeyBoard()
            finish()
        }
        binding.btnSend.makeBounceable()
        binding.btnSend.clickWithDebounce {
            binding.root.hideKeyBoard()
            if (isNetworkConnected())
                sendResetLink()
        }
        binding.inputEmail.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            binding.inputEmail.hideKeyBoard()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendResetLink()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun addObserver() {

        viewModel.settingObserver.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.isSuccess == true) {
                it.message.showSnackBar(this@ForgotPasswordWithEmailActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS,
                        duration = IConstants.SNAKE_BAR_SHOW_TIME)
                mIdlingResource?.setIdleState(true)
                Handler().postDelayed({
                    finish()
                }, IConstants.SNAKE_BAR_SHOW_TIME)
            } else {
                it.message.showSnackBar(this@ForgotPasswordWithEmailActivity)
                mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.validationObserver.observe(this@ForgotPasswordWithEmailActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == EMAIL_EMPTY -> {
                    getString(R.string.alert_enter_email).showSnackBar(this@ForgotPasswordWithEmailActivity)
                }

                it.failType == EMAIL_INVALID -> {
                    getString(R.string.alert_enter_valid_email).showSnackBar(this@ForgotPasswordWithEmailActivity)
                }

                it.failType == EMAIL_LENGTH -> {
                    String.format(getString(R.string.alert_max_email_length),
                            resources.getInteger(R.integer.email_max_length)).showSnackBar(this@ForgotPasswordWithEmailActivity)
                }
            }
        })
    }

    /**
     * Send password reset link on given email address
     */
    private fun sendResetLink() {
        if (viewModel.isValid(binding.inputEmail.getTrimText())) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callForgotPassword(binding.inputEmail.getTrimText())
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
