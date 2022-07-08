package com.app.tyst.ui.home

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.GetPlaidTransactionsRequest
import com.app.tyst.data.model.response.*
import com.app.tyst.data.model.response.plaid.PlaidTransactionsResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.Generics
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.dc.mvvmskeleton.data.model.hb.WSObjectResponse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.home.HomeActivity] and [com.app.tyst.ui.home.HomeFragment]
 */
class HomeRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for application version checking.
     */

    fun callConfigParameters(versionLiveData: MutableLiveData<WSObserverModel<VersionConfigResponse>>) {
        ApiClient.apiService.callConfigParameters().enqueue(object : Callback<WSListResponse<VersionConfigResponse>> {
            override fun onResponse(call: Call<WSListResponse<VersionConfigResponse>>,
                                    response: Response<WSListResponse<VersionConfigResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<VersionConfigResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data?.isNotEmpty() == true) {
                        observerModel.data = body.data?.get(0)
                    }
                    versionLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<VersionConfigResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    versionLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<VersionConfigResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<VersionConfigResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                versionLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for application version checking.
     */

    fun callGetPlaidTransactions(request: GetPlaidTransactionsRequest, transactionLiveData: MutableLiveData<PlaidTransactionsResponse>) {
        ApiClient.plaidApiService.getPlaidTransactions(request).enqueue(object : Callback<PlaidTransactionsResponse> {
            override fun onResponse(call: Call<PlaidTransactionsResponse>,
                                    response: Response<PlaidTransactionsResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    transactionLiveData.value = body
                } else {
//                    val observerModel = WSObserverModel<VersionConfigResponse>()
//                    observerModel.settings =
//                            baseViewModel.getErrorSettings(errorCode = response.code())
                }
            }

            override fun onFailure(call: Call<PlaidTransactionsResponse>, t: Throwable) {
                t.printStackTrace()
//                val observerModel = WSObserverModel<VersionConfigResponse>()
//                observerModel.settings = baseViewModel.getErrorSettings(e = t)
//                versionLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for get all institutions transaction data.
     */

    fun callAllInstitutionsTransactions(map: HashMap<String, String>,
                                        transactionsLiveData: MutableLiveData<WSObserverModel<InstitutionsTransactionResponse>>) {
        ApiClient.apiService.callAllInstitutionsTransactions(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<InstitutionsTransactionResponse>()
                    observerModel.settings = body?.settings
                    if (body?.data != null && body.data is JsonObject) {
                        observerModel.data = Generics.with(body.data!!).getAsObject(InstitutionsTransactionResponse::class.java)
                    }
                    transactionsLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<InstitutionsTransactionResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    transactionsLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<InstitutionsTransactionResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                transactionsLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for fetch transaction details
     */
    fun callFetchTransactions(map: HashMap<String, String>, fetchTransactionData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callFetchTransactions(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    fetchTransactionData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    fetchTransactionData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                fetchTransactionData.value = observerModel
            }
        })
    }

    /**
     * Api call for get all transaction data of an account.
     */

    fun callPlaidSettings(plaidLiveData: MutableLiveData<WSObserverModel<PlaidSettingResponse>>) {

        ApiClient.apiService.callPlaidSettings().apply {
            enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                        response: Response<WSGenericResponse<JsonElement>>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val observerModel = WSObserverModel<PlaidSettingResponse>()
                        observerModel.settings = body?.settings
                        if (body?.data != null && body.data is JsonArray) {
                            observerModel.data = Generics.with(body.data!!).getAsList(Array<PlaidSettingResponse>::class.java)[0]
                        }
                        plaidLiveData.value = observerModel
                    } else {
                        val observerModel = WSObserverModel<PlaidSettingResponse>()
                        observerModel.settings =
                                baseViewModel.getErrorSettings(errorCode = response.code())
                        plaidLiveData.value = observerModel
                    }
                }

                override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                    val observerModel = WSObserverModel<PlaidSettingResponse>()
                    observerModel.settings = baseViewModel.getErrorSettings(e = t)
                    plaidLiveData.value = observerModel
                }
            })
        }
    }

    /**
     * Api call for get subscription status.
     */

    fun callSubscriptionStatus(subscriptionStatusData: MutableLiveData<WSObserverModel<SubscriptionStatusResponse>>) {

        ApiClient.apiService.callSubscriptionStatus().apply {
            enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                        response: Response<WSGenericResponse<JsonElement>>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val observerModel = WSObserverModel<SubscriptionStatusResponse>()
                        observerModel.settings = body?.settings
                        if (body?.data != null && body.data is JsonArray) {
                            observerModel.data = Generics.with(body.data!!).getAsList(Array<SubscriptionStatusResponse>::class.java)[0]
                        }
                        subscriptionStatusData.value = observerModel
                    } else {
                        val observerModel = WSObserverModel<SubscriptionStatusResponse>()
                        observerModel.settings =
                                baseViewModel.getErrorSettings(errorCode = response.code())
                        subscriptionStatusData.value = observerModel
                    }
                }

                override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                    val observerModel = WSObserverModel<SubscriptionStatusResponse>()
                    observerModel.settings = baseViewModel.getErrorSettings(e = t)
                    subscriptionStatusData.value = observerModel
                }
            })
        }
    }


    /**
     * Api Call for Get public token of an institution for re-authenticate it
     */

    fun callGeneratePublicToken(map: HashMap<String, String>,
                            loginMutableLiveData: MutableLiveData<WSObserverModel<PublicTokenResponse>>) {
        ApiClient.apiService.callGeneratePublicToken(map).enqueue(object : Callback<WSListResponse<PublicTokenResponse>> {
            override fun onResponse(call: Call<WSListResponse<PublicTokenResponse>>,
                                    response: Response<WSListResponse<PublicTokenResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<PublicTokenResponse>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data!![0]
                    loginMutableLiveData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<PublicTokenResponse>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    loginMutableLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<PublicTokenResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<PublicTokenResponse>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                loginMutableLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for update institution error status
     */

    fun callUpdateInstitutionErrorLog(map: HashMap<String, String>,
                           updateCategoryLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callUpdateInstitutionErrorLog(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
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