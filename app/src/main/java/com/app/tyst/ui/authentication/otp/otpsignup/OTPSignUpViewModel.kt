package com.app.tyst.ui.authentication.otp.otpsignup

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.OTPResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.ui.authentication.register.SignUpRepository
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.IConstants.Companion.COUNT_DOWN_TIMER
import com.app.tyst.utility.extension.getFailureSettings
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.timeToMinuteSecond
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.dc.retroapi.utils.WebServiceUtils
import com.dc.retroapi.utils.WebServiceUtils.getStringMultipartBodyPart
import okhttp3.RequestBody
import java.util.*

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.otp.otpsignup.OTPSignUpActivity]
 */
class OTPSignUpViewModel(app: Application) : BaseViewModel(app) {
    private val timeLiveData = MutableLiveData<String>()

    private var countDownTimer: CountDownTimer? = null  // Count down timer for 60 sec to enable retry button
    private val enableRetryLiveData = MutableLiveData<Boolean>()

    var otpLiveData = MutableLiveData<WSObserverModel<ArrayList<OTPResponse>>>()
    var signUpLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()
    /**
     * Get timer live data to update timer value in view
     * @return MutableLiveData<String>
     */
    fun getTimerValue(): MutableLiveData<String> {
        return timeLiveData
    }

    fun getEnableRetrySetting(): MutableLiveData<Boolean> {
        return enableRetryLiveData
    }

    /**
     * Start count down timer from 60 second
     */
    fun startTimer() {
        countDownTimer = getCountDownTimer()
        countDownTimer?.start()
    }

    /**
     * Cancel count  down timer
     */
    fun cancelTimer() {
        countDownTimer?.cancel()
    }


    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(COUNT_DOWN_TIMER, 1000) {
            override fun onFinish() {
                timeLiveData.value = ""
                enableRetryLiveData.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
                timeLiveData.value = String.format(app.getString(R.string.lbl_resend_otp_in_minute, millisUntilFinished.timeToMinuteSecond()))
                enableRetryLiveData.value = false
            }
        }
    }

    /**
     * Validate inputs
     */
    fun isValid(otp: String, sendOtp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                settingObserver.value = app.getString(R.string.alert_enter_otp).getFailureSettings()
                false
            }
            otp != sendOtp -> {
                settingObserver.value = app.getString(R.string.alert_invalid_otp).getFailureSettings()
                return false
            }
            else -> true
        }
    }

    /**
     * Api call for send OTP to user phone number
     * @param mobileNumber String User's phone number on which otp will received
     */

    fun callResendOtp(mobileNumber: String) {
        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = ""
        map["mobile_number"] = mobileNumber
        map["user_name"] = ""

        SignUpRepository(this@OTPSignUpViewModel).callCheckUnique(map = map,
                otpLiveData = otpLiveData)
    }

    /**
     * Api call SignUp
     */

    fun callSignUp(request: SignUpRequestModel) {
        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody(request.userName)
        map["first_name"] = WebServiceUtils.getStringRequestBody(request.firstName)
        map["last_name"] = WebServiceUtils.getStringRequestBody(request.lastName)
        map["email"] = WebServiceUtils.getStringRequestBody(request.email)
        map["mobile_number"] = WebServiceUtils.getStringRequestBody(request.mobileNumber)
        map["dob"] = WebServiceUtils.getStringRequestBody(request.dob)
        map["address"] = WebServiceUtils.getStringRequestBody(request.address)
        map["city"] = WebServiceUtils.getStringRequestBody(request.city)
        map["latitude"] = WebServiceUtils.getStringRequestBody(request.latitude)
        map["longitude"] = WebServiceUtils.getStringRequestBody(request.longitude)
        map["state_id"] = WebServiceUtils.getStringRequestBody(request.stateId)
        map["zipcode"] = WebServiceUtils.getStringRequestBody(request.zipCode)
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(sharedPreference.deviceToken
                ?: "")

        if (((app as MainApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE) ||
                (app.getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE_SOCIAL)) {
            if (request.socialType.isEmpty()) {
                map["password"] = WebServiceUtils.getStringRequestBody(request.password)
                SignUpRepository(this@OTPSignUpViewModel).callSignUpWithPhone(map = map,
                        file = if (request.profileImage.isEmpty()) null else getStringMultipartBodyPart("user_profile", request.profileImage),
                        signUpLiveData = signUpLiveData)
            } else {
                map["social_login_type"] = WebServiceUtils.getStringRequestBody(request.socialType)
                map["social_login_id"] = WebServiceUtils.getStringRequestBody(request.socialId)
                SignUpRepository(this@OTPSignUpViewModel).callSignUpWithSocial(map = map,
                        file = if (request.profileImage.isEmpty()) null else getStringMultipartBodyPart("user_profile", request.profileImage),
                        signUpLiveData = signUpLiveData)
            }

        }
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