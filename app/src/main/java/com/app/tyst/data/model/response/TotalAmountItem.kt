package com.app.tyst.data.model.response

import com.app.tyst.utility.extension.toCurrency
import com.google.gson.annotations.SerializedName

data class TotalAmountItem(

        @field:SerializedName("total_transaction_amount")
        val totalTransactionAmount: String? = null,

        @field:SerializedName("total_tax_amount")
        val totalTaxAmount: String? = null
) {
    fun getTransactionAmount() = if (totalTransactionAmount.isNullOrEmpty()) "$0.0" else totalTransactionAmount.toCurrency()

    fun getTaxAmount() = if (totalTaxAmount.isNullOrEmpty()) "$0.0" else totalTaxAmount.toCurrency()
}