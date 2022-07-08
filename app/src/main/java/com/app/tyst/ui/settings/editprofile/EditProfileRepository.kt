package com.app.tyst.ui.settings.editprofile

import androidx.lifecycle.MutableLiveData
import com.app.tyst.MainApplication
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.Generics
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.editprofile.EditProfileActivity] for performing all api callings
 */
class EditProfileRepository(private val baseViewModel: BaseViewModel) {
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

    /**
     * Api call for edit profile.
     */

    fun callEditProfile(map: HashMap<String, RequestBody>, file: MultipartBody.Part?,
                        editLiveData: MutableLiveData<WSObserverModel<UserDetailResponse>>) {
        ApiClient.apiService.callEditProfile(map, file).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data != null && body.data is JsonObject) {
                        observerModel.data = Generics.with(body.data!!).getAsObject(UserDetailResponse::class.java)
                    }
                    editLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<UserDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    editLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<UserDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                editLiveData.value = observerModel
            }
        })
    }
}