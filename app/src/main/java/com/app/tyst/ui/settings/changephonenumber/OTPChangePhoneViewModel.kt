package com.app.tyst.ui.settings.changephonenumber

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.OTPResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.timeToMinuteSecond
import com.app.tyst.utility.validation.OTP_EMPTY
import com.app.tyst.utility.validation.OTP_INVALID
import com.app.tyst.utility.validation.createValidationResult
import com.google.gson.JsonElement
import java.util.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.changephonenumber.OTPChangePhoneActivity]
 */

class OTPChangePhoneViewModel(app: Application) : BaseViewModel(app) {
    private val timeLiveData = MutableLiveData<String>()
    private var countDownTimer: CountDownTimer? = null  // Count down timer for 60 sec to enable retry button
    private val enableRetryLiveData = MutableLiveData<Boolean>()
    var changePhoneNumberData = MutableLiveData<WSObserverModel<JsonElement>>()

    var otpLiveData = MutableLiveData<WSObserverModel<ArrayList<OTPResponse>>>()
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
        return object : CountDownTimer(IConstants.COUNT_DOWN_TIMER, 1000) {
            override fun onFinish() {
                timeLiveData.value = ""
                enableRetryLiveData.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
                timeLiveData.value =  String.format(app.getString(R.string.lbl_resend_otp_in_minute,millisUntilFinished.timeToMinuteSecond() ))
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
                validationObserver.value = createValidationResult(failType = OTP_EMPTY)
                false
            }
            otp != sendOtp -> {
                validationObserver.value = createValidationResult(failType = OTP_INVALID)
                return false
            }
            else -> true
        }
    }

    /**
     * Api call for send OTP to user phone number
     * @param phone String User;s phone number on which otp will received
     */

    fun callResendOTP(type: String, email: String = "", phone: String = "", userName: String = "") {
        val map = HashMap<String, String>()
        map["type"] = type // phone/email
        map["email"] = email
        map["mobile_number"] = phone
        map["user_name"] = userName

        OTPChangePhoneRepository(this@OTPChangePhoneViewModel).callCheckUnique(map = map,
                otpLiveData = otpLiveData)
    }

    /**
     * Api call for change user's phone number
     * @param phone String New Phone Number
     */
    fun callChangePhoneNumber(phone: String){
        val map = HashMap<String, String>()
        map["new_mobile_number"] = phone
        OTPChangePhoneRepository(this@OTPChangePhoneViewModel).callChangePhoneNumber(map = map,
                changePhoneNumberData = changePhoneNumberData)
    }

    /**
     * Clear all user saved data
     */
    fun updatePhoneNumber(phoneNumber: String) {
        val userDetail = sharedPreference.userDetail
        userDetail?.mobileNo = phoneNumber
        sharedPreference.userDetail = userDetail

    }
}