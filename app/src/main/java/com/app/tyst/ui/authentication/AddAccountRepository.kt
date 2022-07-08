package com.app.tyst.ui.authentication

import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for send user bank detail to our server from [com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity]
 * and unlink bank account from [com.app.tyst.ui.myprofile.MyProfileFragment]
 */
class AddAccountRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Api call for upload data
     */
    fun callAddBankAccount(map: HashMap<String, String>, addAccountLiveData: MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>) {
        ApiClient.apiService.callAddBankAccount(map).enqueue(object : Callback<WSListResponse<PlaidInstitutionResponse>> {
            override fun onResponse(call: Call<WSListResponse<PlaidInstitutionResponse>>,
                                    response: Response<WSListResponse<PlaidInstitutionResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    addAccountLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    addAccountLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<PlaidInstitutionResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                addAccountLiveData.value = observerModel
            }
        })
    }

    /**
     * Api call for delete bank account.
     */

    fun callDeleteBankAccount(map: HashMap<String, String>,
                              deleteAccountData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callDeleteBankAccount(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    deleteAccountData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    deleteAccountData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                deleteAccountData.value = observerModel
            }
        })
    }

    /**
     * Api call for get user added accounts
     */
    fun callGetBankAccount(accountsLiveData: MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>) {
        ApiClient.apiService.getUserInstitutions().enqueue(object : Callback<WSListResponse<PlaidInstitutionResponse>> {
            override fun onResponse(call: Call<WSListResponse<PlaidInstitutionResponse>>,
                                    response: Response<WSListResponse<PlaidInstitutionResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    accountsLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    accountsLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<PlaidInstitutionResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<PlaidInstitutionResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                accountsLiveData.value = observerModel
            }
        })
    }
}