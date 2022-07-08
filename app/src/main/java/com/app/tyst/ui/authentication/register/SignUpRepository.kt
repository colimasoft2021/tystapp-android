package com.app.tyst.ui.authentication.register

import androidx.lifecycle.MutableLiveData
import com.app.tyst.MainApplication
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.OTPResponse
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.Generics
import com.app.tyst.data.model.hb.WSGenericResponse
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.dc.mvvmskeleton.data.model.hb.WSObjectResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.register.SignUpActivity] for performing all api callings
 */
class SignUpRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Api call for signup with email.
     */

    fun callSignUpWithEmail(map: HashMap<String, RequestBody>, file: MultipartBody.Part?,
                            signUpLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.callSignUpWithEmail(map, file).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data != null && body.data is JsonObject) {
                        observerModel.data = Generics.with(body.data!!).getAsObject(UserDetailResponse::class.java)
                    }
                    signUpLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    signUpLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<UserDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                signUpLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for signup with phone.
     */

    fun callSignUpWithPhone(map: HashMap<String, RequestBody>, file: MultipartBody.Part?,
                            signUpLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.callSignUpWithPhone(map, file).enqueue(object : Callback<WSObjectResponse<UserDetailResponse>> {
            override fun onResponse(call: Call<WSObjectResponse<UserDetailResponse>>,
                                    response: Response<WSObjectResponse<UserDetailResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    signUpLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    signUpLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSObjectResponse<UserDetailResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<UserDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                signUpLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for signup with social.
     */

    fun callSignUpWithSocial(map: HashMap<String, RequestBody>, file: MultipartBody.Part?,
                             signUpLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.callSignUpWithSocial(map, file).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data != null && body.data is JsonObject) {
                        observerModel.data = Generics.with(body.data!!).getAsObject(UserDetailResponse::class.java)
                    }
                    signUpLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    signUpLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<UserDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                signUpLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for check unique email or phone number.
     */

    fun callCheckUnique(map: HashMap<String, String>,
                        otpLiveData: MutableLiveData<WSObserverModel<ArrayList<OTPResponse>>>) {
        ApiClient.apiService.callCheckUniqueUser(map).enqueue(object : Callback<WSListResponse<OTPResponse>> {


            override fun onResponse(call: Call<WSListResponse<OTPResponse>>,
                                    response: Response<WSListResponse<OTPResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<OTPResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    otpLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ArrayList<OTPResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    otpLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<OTPResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<OTPResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                otpLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for get states list.
     */

    fun callGetStateList(statesLiveData: MutableLiveData<WSObserverModel<ArrayList<StatesResponse>>>) {
        ApiClient.apiService.callGetStateList().enqueue(object : Callback<WSListResponse<StatesResponse>> {


            override fun onResponse(call: Call<WSListResponse<StatesResponse>>,
                                    response: Response<WSListResponse<StatesResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<StatesResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    statesLiveData.value = observerModel
                    (baseViewModel.app as MainApplication).stateList.clear()
                    (baseViewModel.app).stateList.addAll(observerModel.data ?: ArrayList())
                } else {
                    val observerModel = WSObserverModel<ArrayList<StatesResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    statesLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<StatesResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<StatesResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                statesLiveData.value = observerModel
            }
        })
    }
}