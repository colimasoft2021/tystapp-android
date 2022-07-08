package com.app.tyst.ui.settings.staticpages

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.StaticPageResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.staticpages.StaticPagesActivity] for performing all api callings
 */
class StaticPageRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Api call for Static Page.
     */

    fun callStaticPage(map: HashMap<String, String>, staticPageMutableLiveData: MutableLiveData<WSObserverModel<StaticPageResponse>>): Boolean {

        ApiClient.apiService.callStaticPage(map).enqueue(object : Callback<WSListResponse<StaticPageResponse>> {
            override fun onResponse(call: Call<WSListResponse<StaticPageResponse>>,
                                    response: Response<WSListResponse<StaticPageResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<StaticPageResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data?.isNotEmpty() == true) {
                        observerModel.data = body.data?.get(0)
                    }
                    staticPageMutableLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<StaticPageResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    staticPageMutableLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<StaticPageResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<StaticPageResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                staticPageMutableLiveData.value = observerModel
            }
        })
        return true
    }

    /**
     * Call api for update TNC or Privacy Policy
     */
    fun callUpdateTNCPrivacyPolicy(map: HashMap<String, String>) {

        ApiClient.apiService.callUpdateTNCPrivacyPolicy(map)
                .enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                    override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                        baseViewModel.settingObserver.value = baseViewModel.getErrorSettings(e = t)
                    }

                    override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                            response: Response<WSGenericResponse<JsonElement>>) {
                        if (response.isSuccessful) {
                            baseViewModel.settingObserver.value = response.body()?.settings
                        } else {
                            baseViewModel.settingObserver.value =
                                    baseViewModel.getErrorSettings(errorCode = response.code())
                        }
                    }

                })
    }
}