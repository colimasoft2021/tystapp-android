package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Data class hold response of "check_unique_user" api
 * @property otp String?
 * @constructor
 */
data class OTPResponse(
        @field:SerializedName("otp")
        val otp: String? = null)