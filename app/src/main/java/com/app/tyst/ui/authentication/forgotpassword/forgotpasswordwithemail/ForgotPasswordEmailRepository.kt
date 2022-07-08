package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail

import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.data.model.hb.WSGenericResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class for of [com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail.ForgotPasswordWithEmailActivity]] for performing all api callings
 * This repository is used to perform all user's related api calling i.e. login, signup, edit profile etc
 */
class ForgotPasswordEmailRepository(private val baseViewModel: BaseViewModel) {

    /**
     * Call Forgot Password
     */
    fun callForgotPassword(map:HashMap<String, String>) {

        ApiClient.apiService.callForgotPassword(map)
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