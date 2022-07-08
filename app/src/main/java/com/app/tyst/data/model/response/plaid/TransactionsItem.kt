package com.app.tyst.data.model.response.plaid

import com.google.gson.annotations.SerializedName

data class TransactionsItem(

        @field:SerializedName("date")
        val date: String? = null,

        @field:SerializedName("transaction_id")
        val transactionId: String? = null,

        @field:SerializedName("unofficial_currency_code")
        val unofficialCurrencyCode: String? = null,

        @field:SerializedName("amount")
        val amount: Double? = null,

        @field:SerializedName("payment_meta")
        val paymentMeta: PaymentMeta? = null,

        @field:SerializedName("pending")
        val pending: Boolean? = null,

        @field:SerializedName("transaction_type")
        val transactionType: String? = null,

        @field:SerializedName("account_owner")
        val accountOwner: String? = null,

        @field:SerializedName("account_id")
        val accountId: String? = null,

        @field:SerializedName("category_id")
        val categoryId: String? = null,

        @field:SerializedName("iso_currency_code")
        val isoCurrencyCode: String? = null,

        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("location")
        val location: Location? = null,

        @field:SerializedName("pending_transaction_id")
        val pendingTransactionId: String? = null,

        @field:SerializedName("category")
        val category: List<String?>? = null
)