package com.app.tyst.ui.authentication.login.loginwithphonenumber

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.isValidMobileNumber
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.PASSWORD_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_EMPTY
import com.app.tyst.utility.validation.PHONE_NUMBER_INVALID
import com.app.tyst.utility.validation.createValidationResult

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.login.loginwithphonenumber.LoginWithPhoneNumberActivity]
 */
class LoginWithPhoneNumberViewModel(app: Application) : BaseViewModel(app) {

    var loginResponseMutableLiveData = MutableLiveData<WSObserverModel<LoginResponse>>()
    /**
     * Validate login inputs
     */
    fun isValid(phoneNumber: String, password: String): Boolean {
        when {

            phoneNumber.isEmpty() -> {
                validationObserver.value = createValidationResult(PHONE_NUMBER_EMPTY, R.id.inputPhoneNumber)
                return false
            }

            !phoneNumber.isValidMobileNumber() -> {
                validationObserver.value = createValidationResult(PHONE_NUMBER_INVALID, R.id.inputPhoneNumber)
                return false
            }
            password.isEmpty() -> {
                validationObserver.value = createValidationResult(PASSWORD_EMPTY, R.id.inputPassword)
                return false
            }
            else -> return true
        }
    }

    /**
     * Api call login
     */

    fun callLogin(phoneNumber: String,
                  password: String) {
        val map = HashMap<String, String>()
        map["mobile_number"] = phoneNumber
        map["password"] = password
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = sharedPreference.deviceToken ?: ""
        LoginWithPhoneNumberRepository(this@LoginWithPhoneNumberViewModel).callLogin(map = map,
                loginMutableLiveData = loginResponseMutableLiveData)
    }

    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        sharedPreference.isSkip = false
        sharedPreference.userDetail = loginResponse
        sharedPreference.isLogin = true
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
//        sharedPreference.isAdRemoved = loginResponse?.purchaseStatus.equals("Yes")
    }
}