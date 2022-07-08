package com.app.tyst.ui.authentication.login.loginwithemailsocial

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSObjectResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity] for performing all api callings
 */
class LoginWithEmailSocialRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Api call for login.
     */

    fun callLoginWithEmail(map: HashMap<String, String>,
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

    /**
     * Api call for login with social.
     */

    fun callLoginWithSocial(map: HashMap<String, String>,
                            loginMutableLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.loginWithSocial(map).enqueue(object : Callback<WSObjectResponse<UserDetailResponse>> {
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

    fun callSendVerificationLink(map: HashMap<String, String>,sendLinkData: MutableLiveData<WSObserverModel<JsonElement>>) {

        ApiClient.apiService.callSendVerificationLink(map)
                .enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                    override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                        val observerModel = WSObserverModel<JsonElement>()
                        observerModel.settings = baseViewModel.getErrorSettings(e = t)
                        sendLinkData.value = observerModel
                    }

                    override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                            response: Response<WSGenericResponse<JsonElement>>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            val observerModel = WSObserverModel<JsonElement>()
                            observerModel.settings = body?.settings
                            observerModel.data = body?.data
                            sendLinkData.value = observerModel

                        } else {
                            val observerModel = WSObserverModel<JsonElement>()
                            observerModel.settings =
                                    baseViewModel.getErrorSettings(errorCode = response.code())
                            sendLinkData.value = observerModel
                        }
                    }

                })
    }

}
