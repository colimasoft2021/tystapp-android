package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class PaymentMeta(

	@field:SerializedName("payee")
	val payee: String? = null,

	@field:SerializedName("ppd_id")
	val ppdId: String? = null,

	@field:SerializedName("reason")
	val reason: String? = null,

	@field:SerializedName("by_order_of")
	val byOrderOf: String? = null,

	@field:SerializedName("payment_processor")
	val paymentProcessor: String? = null,

	@field:SerializedName("payer")
	val payer: String? = null,

	@field:SerializedName("reference_number")
	val referenceNumber: Any? = null,

	@field:SerializedName("payment_method")
	val paymentMethod: String? = null
)