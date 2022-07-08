package com.app.tyst.ui.settings.changephonenumber

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
import com.app.tyst.R
import com.app.tyst.databinding.ActivityChangePhoneNumberBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID

class ChangePhoneNumberActivity : BaseActivity() {

    lateinit var binding: ActivityChangePhoneNumberBinding
    private val viewModel: ChangePhoneNumberViewModel
        get() = ViewModelProviders.of(this).get(ChangePhoneNumberViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@ChangePhoneNumberActivity, R.layout.activity_change_phone_number)
        initView()
    }

    private fun initView() {
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        initListeners()
        addObserver()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnSend.makeBounceable()
            btnSend.clickWithDebounce{ callCheckUnique(PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText())) }

            inputPhoneNumber.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                binding.inputPhoneNumber.hideKeyBoard()
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    callCheckUnique(PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()))
                    return@OnEditorActionListener true
                }
                false
            })
        }
    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@ChangePhoneNumberActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when {
                it.failType == PHONE_NUMBER_EMPTY -> {
                    getString(R.string.alert_enter_mobile_number).showSnackBar(this@ChangePhoneNumberActivity)
                }

                it.failType == PHONE_NUMBER_INVALID -> {
                    getString(R.string.alert_invalid_phone_number).showSnackBar(this@ChangePhoneNumberActivity)
                }
            }
        })


        viewModel.otpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                mIdlingResource?.setIdleState(true)
                it.settings!!.message.showSnackBar(this@ChangePhoneNumberActivity,
                        IConstants.SNAKBAR_TYPE_SUCCESS,
                        duration = IConstants.SNAKE_BAR_SHOW_TIME)
                startActivity(OTPChangePhoneActivity.getStartIntent(context = this@ChangePhoneNumberActivity,
                        phoneNumber = binding.inputPhoneNumber.getTrimText(),
                        otp = it.data?.get(0)?.otp ?: ""))
            } else if (!handleApiError(it.settings)) {
                mIdlingResource?.setIdleState(true)
                it?.settings?.message?.showSnackBar(this@ChangePhoneNumberActivity)
            }
        })

    }

    private fun callCheckUnique(phone: String) {
        if (viewModel.isValid(phone)) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callCheckUnique(type = "phone", phone = phone)
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
