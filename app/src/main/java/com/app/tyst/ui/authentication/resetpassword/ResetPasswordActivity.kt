package com.app.tyst.ui.authentication.resetpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.ActivityResetPasswordBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.*

/**
 * This activity is used to reset password if user use "Forgot Password With Phone Number"
 */
class ResetPasswordActivity : BaseActivity() {

    companion object {

        fun getStartIntent(context: Context, phoneNumber: String, resetKey: String): Intent {
            return Intent(context, ResetPasswordActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("resetKey", resetKey)
            }
        }
    }

    lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: ResetPasswordViewModel
        get() = ViewModelProviders.of(this).get(ResetPasswordViewModel::class.java)
    private var resetKey = ""
    private var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ResetPasswordActivity, R.layout.activity_reset_password)
        resetKey = intent?.getStringExtra("resetKey") ?: ""
        phoneNumber = intent?.getStringExtra("phoneNumber") ?: ""
        initView()
    }

    private fun initView() {
        initListeners()
        addObserver()
    }

    private fun addObserver() {
        viewModel.settingObserver.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.isSuccess == true) {
                it.message.showSnackBar(this@ResetPasswordActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS,
                        duration = IConstants.SNAKE_BAR_SHOW_TIME)
                Handler().postDelayed({ navigateToLoginScreen(false) }, IConstants.SNAKE_BAR_SHOW_TIME)
            } else {
                it.message.showSnackBar(this@ResetPasswordActivity)

            }
            mIdlingResource?.setIdleState(true)

        })

        viewModel.validationObserver.observe(this@ResetPasswordActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_new_password).showSnackBar(this@ResetPasswordActivity)
                }

                it.failType == PASSWORD_INVALID -> {
                    getString(R.string.alert_valid_password).showSnackBar(this@ResetPasswordActivity)
                }

                it.failType == CONFORM_PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_confirm_password).showSnackBar(this@ResetPasswordActivity)
                }

                it.failType == PASSWORD_NOT_MATCH -> {
                    getString(R.string.alert_confirm_password_not_match).showSnackBar(this@ResetPasswordActivity)
                }
            }
        })
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnReset.makeBounceable()
            btnReset.clickWithDebounce {
                binding.root.hideKeyBoard()
                if (isNetworkConnected())
                    performResetPassword()

            }
        }
    }

    private fun performResetPassword() {
        if (viewModel.isValid(newPassword = binding.inputNewPassword.getTrimText(),
                        confirmPassword = binding.inputConfirmPassword.getTrimText())) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callResetPassword(newPassword = binding.inputNewPassword.getTrimText(),
                    phoneNumber = phoneNumber, resetKey = resetKey)
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
