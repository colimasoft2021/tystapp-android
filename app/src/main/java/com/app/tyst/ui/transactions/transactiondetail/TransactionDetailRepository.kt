package com.app.tyst.ui.transactions.transactiondetail

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.TransactionDetailResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.transactions.transactiondetail.TransactionDetailActivity]
 */
class TransactionDetailRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Api call for get Transaction detail.
     */

    fun callTransactionDetail(map: HashMap<String, String>,
                              loginMutableLiveData: MutableLiveData<WSObserverModel<TransactionDetailResponse>>) {
        ApiClient.apiService.callTransactionDetail(map).enqueue(object : Callback<WSListResponse<TransactionDetailResponse>> {
            override fun onResponse(call: Call<WSListResponse<TransactionDetailResponse>>,
                                    response: Response<WSListResponse<TransactionDetailResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<TransactionDetailResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data?.isNotEmpty() == true) {
                        observerModel.data = body.data?.get(0)
                    }
                    loginMutableLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<TransactionDetailResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    loginMutableLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<TransactionDetailResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<TransactionDetailResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                loginMutableLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for change tax.
     */

    fun callChangeTax(map: HashMap<String, String>,
                      changeTaxLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callChangeTax(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    changeTaxLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    changeTaxLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                changeTaxLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for add tip.
     */

    fun callAddTip(map: HashMap<String, String>,
                   tipAddedLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callAddTip(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    tipAddedLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    tipAddedLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                tipAddedLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for delete transaction.
     */

    fun callDeleteTransaction(map: HashMap<String, String>,
                              deleteTransactionLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callDeleteTransaction(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    deleteTransactionLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    deleteTransactionLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                deleteTransactionLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for delete transaction.
     */

    fun callUpdateCategory(map: HashMap<String, String>,
                              updateCategoryLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callUpdateCategory(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    updateCategoryLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    updateCategoryLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                updateCategoryLiveData.value = observerModel
            }
        })
    }
}