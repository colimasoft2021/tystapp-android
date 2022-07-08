package com.app.tyst.ui.authentication.login.loginwithemail

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.dc.mvvmskeleton.data.model.hb.WSObjectResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.login.loginwithemail.LoginWithEmailActivity] for performing all api callings
 */
class LoginWithEmailRepository (private val baseViewModel: BaseViewModel) {
    /**
     * Api call for login.
     */

    fun callLogin(map: HashMap<String, String>,
                           loginMutableLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.loginWithEmail(map).enqueue(object : Callback<WSObjectResponse<UserDetailResponse>> {
            override fun onResponse(call: Call<WSObjectResponse<UserDetailResponse>>,
                                    response: Response<WSObjectResponse<UserDetailResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    loginMutableLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    loginMutableLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSObjectResponse<UserDetailResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<UserDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                loginMutableLiveData.value = observerModel
            }
        })
    }
}