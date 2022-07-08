package com.app.tyst.data.model.request

data class GetPlaidTransactionsRequest(

	val access_token: String = "",

	val end_date: String? = null,

//	@field:SerializedName("options")
//	val options: Options? = null,

	val secret: String = "",

	val client_id: String = "",

	val start_date: String = ""
)