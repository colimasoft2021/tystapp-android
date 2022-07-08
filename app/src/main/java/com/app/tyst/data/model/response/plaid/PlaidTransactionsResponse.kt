package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class PlaidTransactionsResponse(

	@field:SerializedName("item")
	val item: Item? = null,

	@field:SerializedName("total_transactions")
	val totalTransactions: Double? = null,

	@field:SerializedName("accounts.json")
	val accounts: List<AccountsItem?>? = null,

	@field:SerializedName("transactions")
	val transactions: List<TransactionsItem?>? = null,

	@field:SerializedName("request_id")
	val requestId: String? = null
)