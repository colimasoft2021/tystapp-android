package com.app.tyst.ui.transactions.receipt

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.CategoryResponse
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
 * Repository class for of [com.app.tyst.ui.transactions.receipt.AddReceiptActivity] for performing all api callings
 */
class AddReceiptRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for Add receipt.
     */

    fun callPayByCash(map: HashMap<String, RequestBody>, files: List<MultipartBody.Part>?,
                      addReceiptData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callPayByCash(map, files).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    addReceiptData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    addReceiptData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                addReceiptData.value = observerModel
            }
        })
    }

    /**
     * Api call for get states list.
     */

    fun callGetCategoryList(categoryData: MutableLiveData<WSObserverModel<ArrayList<CategoryResponse>>>) {
        ApiClient.apiService.callCategoryList().enqueue(object : Callback<WSListResponse<CategoryResponse>> {


            override fun onResponse(call: Call<WSListResponse<CategoryResponse>>,
                                    response: Response<WSListResponse<CategoryResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<CategoryResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    categoryData.value = observerModel
//                    (baseViewModel.app).stateList.addAll(observerModel.data ?: ArrayList())
                } else {
                    val observerModel = WSObserverModel<ArrayList<CategoryResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    categoryData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<CategoryResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<CategoryResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                categoryData.value = observerModel
            }
        })
    }
}