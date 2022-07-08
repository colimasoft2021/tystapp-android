package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class PlaidSettingResponse(

	@field:SerializedName("public_key")
	val publicKey: String? = null,

	@field:SerializedName("secret_key")
	val secretKey: String? = null,

	@field:SerializedName("curl_base_url")
	val curlBaseUrl: String? = null,

	@field:SerializedName("client_id")
	val clientId: String? = null
)