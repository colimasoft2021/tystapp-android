package com.app.tyst.ui.settings.changepassword

import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.data.model.hb.WSGenericResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.changepassword.ChangePasswordActivity]] for performing all api callings
 */
class ChangePasswordRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Call Change Password
     */
    fun callChangePassword(map: HashMap<String, String>) {

        ApiClient.apiService.callChangePassword(map)
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