package com.app.tyst.ui.authentication.resetpassword

import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.data.model.hb.WSGenericResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.resetpassword.ResetPasswordActivity]] for performing all api callings
 */
class ResetPasswordRepository(private val baseViewModel: BaseViewModel) {
    /**
     * Call Reset Password
     */
    fun callResetPassword(map: HashMap<String, String>) {

        ApiClient.apiService.callResetPassword(map)
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