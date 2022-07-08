package com.app.tyst.ui.notification

import com.app.tyst.MainApplication
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.utility.NotificationUtils
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.data.model.hb.WSGenericResponse
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(deviceToken: String) {
        super.onNewToken(deviceToken)
        LOGApp.d("NEW_TOKEN", deviceToken)
        sendRegistrationTokenToServer(deviceToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {

            LOGApp.e("Notification", remoteMessage.data.toString())

//            val jsonObject = JSONObject(remoteMessage.data["others"])


            NotificationUtils.createAppNotification(this, application as MainApplication, remoteMessage)
        }
//
    }

    private fun sendRegistrationTokenToServer(token: String) {
        sharedPreference.deviceToken = token
        if (sharedPreference.authToken.isNullOrEmpty())
            return
        ApiClient.apiService.callUpdateDeviceToken(sharedPreference.deviceToken?:"")
                .enqueue(object : Callback<WSGenericResponse<JsonElement>> {
                    override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                            response: Response<WSGenericResponse<JsonElement>>) {

                    }

                    override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {

                    }
                })

    }

}
