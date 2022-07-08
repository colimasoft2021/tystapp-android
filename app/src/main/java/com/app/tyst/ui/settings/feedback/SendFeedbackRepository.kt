package com.app.tyst.ui.settings.feedback

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.data.model.hb.WSGenericResponse
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendFeedbackRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for Send Feedback
     * @param feedbackLiveData MutableLiveData<WSObserverModel<JsonElement>>
     */
    fun callSendFeedback(map: HashMap<String, RequestBody>, files: List<MultipartBody.Part>?,feedbackLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {

        ApiClient.apiService.callSendFeedback(map,files).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    feedbackLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    feedbackLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                feedbackLiveData.value = observerModel
            }
        })
    }

}