package com.app.tyst.ui.transactions.changetaxandtip

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.ReasonListResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.transactions.changetaxandtip.ChangeTaxBottomSheetDialog] for performing all api callings
 */
class ChangeTaxRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for get reason list.
     */

    fun callGetReasonList(categoryData: MutableLiveData<WSObserverModel<ArrayList<ReasonListResponse>>>) {
        ApiClient.apiService.callReasonList().enqueue(object : Callback<WSListResponse<ReasonListResponse>> {


            override fun onResponse(call: Call<WSListResponse<ReasonListResponse>>,
                                    response: Response<WSListResponse<ReasonListResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<ReasonListResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    categoryData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ArrayList<ReasonListResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    categoryData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<ReasonListResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<ReasonListResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                categoryData.value = observerModel
            }
        })
    }
}