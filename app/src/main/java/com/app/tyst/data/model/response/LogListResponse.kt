package com.app.tyst.data.model.response

import com.app.tyst.utility.extension.fromServerDateToYYYYMMDD
import com.app.tyst.utility.extension.toCurrency
import com.google.gson.annotations.SerializedName

data class LogListResponse(

        @field:SerializedName("end_date")
        val endDate: String? = null,

        @field:SerializedName("added_at")
        val addedAt: String? = null,

        @field:SerializedName("total_transaction_amount")
        val totalTransactionAmount: String? = null,

        @field:SerializedName("log_file")
        val logFile: String? = null,

        @field:SerializedName("total_tax_amount")
        val totalTaxAmount: String? = null,

        @field:SerializedName("start_date")
        val startDate: String? = null,

        @field:SerializedName("status")
        val status: String? = null
) {
    /**
     * Convert amount from String to Currency format
     * @return String
     */
    fun parseTransactionAmount() = "Total Amount: "+ if (totalTransactionAmount.isNullOrEmpty()) "0.0" else totalTransactionAmount.toCurrency()

    /**
     * Convert amount from String to Currency format
     * @return String
     */
    fun parseTaxAmount() = "Tax Applied: "+if (totalTaxAmount.isNullOrEmpty()) "0.0" else totalTaxAmount.toCurrency()

    fun parseStartDate() = startDate?.fromServerDateToYYYYMMDD()
    fun parseEndate() = endDate?.fromServerDateToYYYYMMDD()

    fun getDurationDate() = parseStartDate() + " - " + parseEndate()

    fun getSalesAmount() = "Sales Amount: "+((totalTransactionAmount?.toDoubleOrNull()
            ?: 0.0) - (totalTaxAmount?.toDoubleOrNull() ?: 0.0)).toCurrency()
}