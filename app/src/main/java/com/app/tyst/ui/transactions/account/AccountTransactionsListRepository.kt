package com.app.tyst.ui.transactions.account

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.AccountTransactionsDataResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.Generics
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.transactions.account.AccountTransactionsListActivity]
 */
class AccountTransactionsListRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for get all transaction data of an account.
     */

    fun callTransactionData(map: HashMap<String, String>,
                            transactionsLiveData: MutableLiveData<WSObserverModel<AccountTransactionsDataResponse>>): Call<WSGenericResponse<JsonElement>> {

        return ApiClient.apiService.callTransactionData(map).apply {
            enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                        response: Response<WSGenericResponse<JsonElement>>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val observerModel = WSObserverModel<AccountTransactionsDataResponse>()
                        observerModel.settings = body?.settings
                        if (body?.data != null && body.data is JsonObject) {
                            observerModel.data = Generics.with(body.data!!).getAsObject(AccountTransactionsDataResponse::class.java)
                        }
                        transactionsLiveData.value = observerModel
                    } else {
                        val observerModel = WSObserverModel<AccountTransactionsDataResponse>()
                        observerModel.settings =
                                baseViewModel.getErrorSettings(errorCode = response.code())
                        transactionsLiveData.value = observerModel
                    }
                }

                override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                    val observerModel = WSObserverModel<AccountTransactionsDataResponse>()
                    observerModel.settings = baseViewModel.getErrorSettings(e = t)
                    transactionsLiveData.value = observerModel
                }
            })
        }
    }
}