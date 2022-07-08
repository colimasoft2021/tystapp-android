package com.app.tyst.ui.settings.changepassword

import android.os.Bundle
import android.os.Handler
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.databinding.ActivityChangePasswordBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.*

class ChangePasswordActivity : BaseActivity() {

    lateinit var binding: ActivityChangePasswordBinding

    private val viewModel: ChangePasswordViewModel
        get() = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ChangePasswordActivity, R.layout.activity_change_password)
        initView()
    }

    private fun initView() {
        initListeners()
        addObserver()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnUpdate.makeBounceable()
            btnUpdate.clickWithDebounce { changePassword() }
        }
    }

    private fun addObserver() {
        viewModel.settingObserver.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.isSuccess == true) {
                it.message.showSnackBar(this@ChangePasswordActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS,
                        duration = IConstants.SNAKE_BAR_SHOW_TIME)
                Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
            } else {
                it.message.showSnackBar(this@ChangePasswordActivity)
            }

            mIdlingResource?.setIdleState(true)
        })

        viewModel.validationObserver.observe(this@ChangePasswordActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == OLD_PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_old_password).showSnackBar(this@ChangePasswordActivity)
                }

                it.failType == PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_new_password).showSnackBar(this@ChangePasswordActivity)
                }

                it.failType == PASSWORD_INVALID -> {
                    getString(R.string.alert_valid_password).showSnackBar(this@ChangePasswordActivity)
                }

                it.failType == CONFORM_PASSWORD_EMPTY -> {
                    getString(R.string.alert_enter_confirm_password).showSnackBar(this@ChangePasswordActivity)
                }

                it.failType == PASSWORD_NOT_MATCH -> {
                    getString(R.string.alert_confirm_password_not_match).showSnackBar(this@ChangePasswordActivity)
                }
            }
        })
    }

    /**
     * Perform change password.
     * Check Internet connection and validation of input fields.
     * If all is ok, then all api to change password
     */
    private fun changePassword() {
        binding.root.hideKeyBoard()
        if (isNetworkConnected()) {
            if (viewModel.isValid(oldPassword = binding.inputOldPassword.getTrimText(),
                            newPassword = binding.inputNewPassword.getTrimText(),
                            confirmPassword = binding.inputConfirmPassword.getTrimText())) {
                showHideProgressDialog(true)
                mIdlingResource?.setIdleState(false)
                viewModel.callChangePassword(oldPassword = binding.inputOldPassword.getTrimText(),
                        newPassword = binding.inputNewPassword.getTrimText(),isEncrypted = (application as MainApplication).isEncryptionApply)
            }

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
