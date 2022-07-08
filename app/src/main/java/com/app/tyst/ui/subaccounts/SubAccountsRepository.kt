package com.app.tyst.ui.subaccounts

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.AccountResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.subaccounts]
 */
class SubAccountsRepository (private val baseViewModel: BaseViewModel) {

    /**
     * Api call for get all sub accounts of an institution.
     */

    fun callUserAccounts(map: HashMap<String, String>,
                         accountLiveData:  MutableLiveData<WSObserverModel<ArrayList<AccountResponse>>>) {
        ApiClient.apiService.callUserAccounts(map).enqueue(object : Callback<WSListResponse<AccountResponse>> {

            override fun onResponse(call: Call<WSListResponse<AccountResponse>>,
                                    response: Response<WSListResponse<AccountResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<AccountResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    accountLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ArrayList<AccountResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    accountLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<AccountResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<AccountResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                accountLiveData.value = observerModel
            }
        })
    }
}