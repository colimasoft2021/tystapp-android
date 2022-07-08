package com.app.tyst.ui.authentication.login.loginwithemail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.utility.extension.isValidEmail
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.PASSWORD_EMPTY
import com.app.tyst.utility.validation.createValidationResult

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.login.loginwithemail.LoginWithEmailActivity]
 */
class LoginWithEmailViewModel(app: Application) : BaseViewModel(app) {

    var loginResponseMutableLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()

    /**
     * Validate login inputs
     */
    fun isValid(password: String, email: String): Boolean {
        when {
            email.isEmpty() -> {
                validationObserver.value = createValidationResult(EMAIL_EMPTY, R.id.inputEmail)
                return false
            }

            !email.isValidEmail() -> {
                validationObserver.value = createValidationResult(EMAIL_INVALID, R.id.inputEmail)
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

    fun callLogin(email: String, password: String) {
        val map = HashMap<String, String>()
        map["email"] = email
        map["password"] = password
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = sharedPreference.deviceToken ?: ""
        LoginWithEmailRepository(this@LoginWithEmailViewModel).callLogin(map = map,
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