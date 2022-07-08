package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class Balances(

	@field:SerializedName("unofficial_currency_code")
	val unofficialCurrencyCode: Any? = null,

	@field:SerializedName("current")
	val current: Double? = null,

	@field:SerializedName("available")
	val available: Double? = null,

	@field:SerializedName("iso_currency_code")
	val isoCurrencyCode: String? = null,

	@field:SerializedName("limit")
	val limit: Int? = null
)