package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class SubscriptionStatusResponse(

        @field:SerializedName("is_subscribed")
        val isSubscribed: String? = null
)