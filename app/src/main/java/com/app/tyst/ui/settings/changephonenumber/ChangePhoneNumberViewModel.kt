package com.app.tyst.ui.settings.changephonenumber

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.OTPResponse
import com.app.tyst.ui.authentication.register.SignUpRepository
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.extension.isValidMobileNumber
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID
import com.app.tyst.utility.validation.createValidationResult

/**
 * View model call for performing all business logic of [com.app.tyst.ui.changephonenumber.ChangePhoneNumberActivity]
 */
class ChangePhoneNumberViewModel(app: Application) : BaseViewModel(app) {
    var otpLiveData = MutableLiveData<WSObserverModel<ArrayList<OTPResponse>>>()
    /**
     * Api call for check unique email or phone number.
     * @param type String phone/email  phone -> If phone number unique check, email -> If email unique check
     * @param email String user's email which need to check
     * @param phone String user's phone which need to check
     */

    fun callCheckUnique(type: String, email: String = "", phone: String = "", userName: String = "") {
        val map = HashMap<String, String>()
        map["type"] = type // phone/email
        map["email"] = email
        map["mobile_number"] = phone
        map["user_name"] = userName

        SignUpRepository(this@ChangePhoneNumberViewModel).callCheckUnique(map = map,
                otpLiveData = otpLiveData)
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