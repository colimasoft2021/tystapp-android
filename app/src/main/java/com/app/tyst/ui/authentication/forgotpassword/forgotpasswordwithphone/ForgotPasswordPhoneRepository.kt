package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone

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
 * Repository class for of [com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithphone]] for performing all api callings
 */
class ForgotPasswordPhoneRepository(private val baseViewModel: BaseViewModel) {

    /**
     *  Call Forgot Password with phone number
     * @param map HashMap<String, String> api input parameters
     * @param forgotPasswordLiveData MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>
     */
    fun callForgotPassword(map:HashMap<String, String>, forgotPasswordLiveData: MutableLiveData<WSObserverModel<ForgotPasswordPhoneResponse>>) {

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
                    forgotPasswordLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ForgotPasswordPhoneResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    forgotPasswordLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<ForgotPasswordPhoneResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ForgotPasswordPhoneResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                forgotPasswordLiveData.value = observerModel
            }
        })
    }
}