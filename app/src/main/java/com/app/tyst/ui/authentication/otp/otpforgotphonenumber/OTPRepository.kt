package com.app.tyst.ui.authentication.otp.otpforgotphonenumber

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.ForgotPasswordPhoneResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.otp.OTPActivity]] for performing all api callings
 */
class OTPRepository (private val baseViewModel: BaseViewModel){
    /**
     *  Call Forgot Password with phone number
     * @param map HashMap<String, String> api input parameters
     * @param resendOtpLiveData MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>
     */
    fun callResendOTP(map:HashMap<String, String>, resendOtpLiveData: MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>) {

        ApiClient.apiService.callForgotPasswordWithPhone(map).enqueue(object : Callback<WSListResponse<ForgotPasswordPhoneResponse>> {
            override fun onResponse(call: Call<WSListResponse<ForgotPasswordPhoneResponse>>,
                                    response: Response<WSListResponse<ForgotPasswordPhoneResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ForgotPasswordPhoneResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data?.isNotEmpty() == true) {
                        observerModel.data = body.data?.get(0)
                    }
                    resendOtpLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ForgotPasswordPhoneResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    resendOtpLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<ForgotPasswordPhoneResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ForgotPasswordPhoneResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                resendOtpLiveData.value = observerModel
            }
        })
    }
}