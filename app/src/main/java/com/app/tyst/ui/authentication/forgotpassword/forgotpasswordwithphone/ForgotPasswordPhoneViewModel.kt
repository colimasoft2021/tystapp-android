package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.ForgotPasswordPhoneResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.extension.isValidMobileNumber
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID
import com.app.tyst.utility.validation.createValidationResult

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone.ForgotPasswordWithPhoneActivity]
 */
class ForgotPasswordPhoneViewModel(app: Application) : BaseViewModel(app) {
    var forgotPasswordLiveData = MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>()
    /**
     * Api call send reset password link on user's registered email id
     */

    fun callForgotPassword(phone: String) {
        val map = HashMap<String, String>()
        map["mobile_number"] = phone
        ForgotPasswordPhoneRepository(this@ForgotPasswordPhoneViewModel).callForgotPassword(map = map,
                forgotPasswordLiveData = forgotPasswordLiveData)
    }

    /**
     * Validate inputs
     */
    fun isValid(phoneNumber: String): Boolean {
        return when {

            phoneNumber.isEmpty() -> {
                validationObserver.value = createValidationResult(PHONE_NUMBER_EMPTY, R.id.inputPhoneNumber)
                false
            }

            !phoneNumber.isValidMobileNumber() -> {
                validationObserver.value = createValidationResult(PHONE_NUMBER_INVALID, R.id.inputPhoneNumber)
                return false
            }
            else -> true
        }
    }

}