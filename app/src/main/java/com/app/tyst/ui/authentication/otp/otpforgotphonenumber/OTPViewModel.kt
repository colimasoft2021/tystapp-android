package com.app.tyst.ui.authentication.otp.otpforgotphonenumber

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.ForgotPasswordPhoneResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants.Companion.COUNT_DOWN_TIMER
import com.app.tyst.utility.extension.timeToMinuteSecond
import com.app.tyst.utility.validation.OTP_EMPTY
import com.app.tyst.utility.validation.OTP_INVALID
import com.app.tyst.utility.validation.createValidationResult
import java.util.*

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.otp.otpsignup.OTPSignUpActivity]
 */
class OTPViewModel(app: Application) : BaseViewModel(app) {
    private val timeLiveData = MutableLiveData<String>()
    private var countDownTimer: CountDownTimer? = null  // Count down timer for 60 sec to enable retry button
    private val enableRetryLiveData = MutableLiveData<Boolean>()
    var resendOtpLiveData = MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>()
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
    fun cancelTimer(markFinish:Boolean = false) {
        countDownTimer?.cancel()
        if(markFinish)
            countDownTimer?.onFinish()
    }
    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(COUNT_DOWN_TIMER, 1000) {
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

    fun callForgotPassword(phone: String) {
        val map = HashMap<String, String>()
        map["mobile_number"] = phone
        OTPRepository(this@OTPViewModel).callResendOTP(map = map,
                resendOtpLiveData = resendOtpLiveData)
    }
}