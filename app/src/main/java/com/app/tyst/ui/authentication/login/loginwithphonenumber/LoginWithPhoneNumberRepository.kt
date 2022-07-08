package com.app.tyst.ui.authentication.login.loginwithphonenumber

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.login.loginwithphonenumber.LoginWithPhoneNumberActivity] for performing all api callings
 */
class LoginWithPhoneNumberRepository (private val baseViewModel: BaseViewModel) {
    /**
     * Api call for login.
     */

    fun callLogin(map: HashMap<String, String>,
                  loginMutableLiveData: MutableLiveData<WSObserverModel<LoginResponse>>) {
        ApiClient.apiService.loginWithPhone(map).enqueue(object : Callback<WSListResponse<LoginResponse>> {
            override fun onResponse(call: Call<WSListResponse<LoginResponse>>,
                                    response: Response<WSListResponse<LoginResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<LoginResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data?.isNotEmpty() == true) {
                        observerModel.data = body.data?.get(0)
                    }
                    loginMutableLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<LoginResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    loginMutableLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<LoginResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<LoginResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                loginMutableLiveData.value = observerModel
            }
        })
    }
}