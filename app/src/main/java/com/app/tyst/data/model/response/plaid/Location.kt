package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class Location(

	@field:SerializedName("country")
	val country: Any? = null,

	@field:SerializedName("address")
	val address: Any? = null,

	@field:SerializedName("city")
	val city: Any? = null,

	@field:SerializedName("store_number")
	val storeNumber: Any? = null,

	@field:SerializedName("lon")
	val lon: Any? = null,

	@field:SerializedName("postal_code")
	val postalCode: Any? = null,

	@field:SerializedName("region")
	val region: Any? = null,

	@field:SerializedName("lat")
	val lat: Any? = null
)