package com.app.tyst.ui.settings.changephonenumber

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.OTPResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.data.model.hb.WSGenericResponse
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.changephonenumber.ChangePhoneNumberActivity] for performing all api callings
 */

class OTPChangePhoneRepository  (private val baseViewModel: BaseViewModel){
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
     * Api call for change mobile number
     */
    fun callChangePhoneNumber(map: HashMap<String, String>, changePhoneNumberData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callChangePhoneNumber(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    changePhoneNumberData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    changePhoneNumberData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                changePhoneNumberData.value = observerModel
            }
        })
    }
}