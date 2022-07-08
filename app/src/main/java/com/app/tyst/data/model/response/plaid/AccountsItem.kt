package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class AccountsItem(

	@field:SerializedName("official_name")
	val officialName: String? = null,

	@field:SerializedName("balances")
	val balances: Balances? = null,

	@field:SerializedName("account_id")
	val accountId: String? = null,

	@field:SerializedName("subtype")
	val subtype: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("mask")
	val mask: String? = null
)