package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Data class hold response of "forgot_password_phone" api
 * @property otp String? Otp
 * @property resetKey String? reset key
 * @constructor
 */
data class ForgotPasswordPhoneResponse(

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("reset_key")
	val resetKey: String? = null
)