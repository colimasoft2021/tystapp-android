package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class Item(

	@field:SerializedName("webhook")
	val webhook: String? = null,

	@field:SerializedName("item_id")
	val itemId: String? = null,

	@field:SerializedName("billed_products")
	val billedProducts: List<String?>? = null,

	@field:SerializedName("error")
	val error: Any? = null,

	@field:SerializedName("available_products")
	val availableProducts: List<String?>? = null,

	@field:SerializedName("institution_id")
	val institutionId: String? = null
)