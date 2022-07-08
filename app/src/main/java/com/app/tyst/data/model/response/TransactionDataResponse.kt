package com.app.tyst.data.model.response

import com.app.tyst.R
import com.app.tyst.utility.extension.fromServerDateToYYYYMMDD
import com.app.tyst.utility.extension.toCurrency
import com.google.gson.annotations.SerializedName

/**
 * This data class is used for parse transaction data of an account
 */
data class TransactionDataResponse(

        @field:SerializedName("tax_amount")
        val taxAmount: String? = null,

        @field:SerializedName("t_transaction_id")
        val tTransactionId: String? = null,

        @field:SerializedName("transaction_amount")
        val transactionAmount: String? = null,

        @field:SerializedName("latitude")
        val latitude: String? = null,

        @field:SerializedName("receipt_image")
        val receiptImage: String? = null,

        @field:SerializedName("institution_name")
        val institutionName: String? = null,

        @field:SerializedName("zip_code")
        val zipCode: String? = null,

        @field:SerializedName("institution_id")
        val institutionId: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("pay_by_cash")
        val payByCash: String? = null,

        @field:SerializedName("store_name")
        val storeName: String? = null,

        @field:SerializedName("state_id")
        val stateId: String? = null,

        @field:SerializedName("state")
        val state: String? = null,

        @field:SerializedName("longitude")
        val longitude: String? = null,

        @field:SerializedName("transaction_date")
        val transactionDate: String? = null,

        @field:SerializedName("transaction_id")
        val transactionId: String? = null,

        @field:SerializedName("is_modified_transaction")
        val isModifiedTransaction: String? = null,

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("added_at")
        val addedAt: String? = null,

        @field:SerializedName("account_id")
        val accountId: String? = null,

        @field:SerializedName("user_id")
        val userId: String? = null,

        @field:SerializedName("taxperc")
        val taxperc: String? = null,

        @field:SerializedName("location")
        val location: String? = null,

        @field:SerializedName("category")
        val category: String? = null,

        @field:SerializedName("status")
        val status: String? = null,

        @field:SerializedName("account_name")
        val accountName: String? = null,

        @field:SerializedName("account_subtype")
        val accountSubtype: String? = null,

        @field:SerializedName("account_type")
        val accountType: String? = null,

        @field:SerializedName("reason_id")
        val reasonId: String? = null,

        @field:SerializedName("changed_tax_amount")
        val changedTaxAmount: String? = null,

        @field:SerializedName("reason_text")
        val reasonText: String? = null
) {
    /**
     * Convert amount from String to Currency format
     * @return String
     */
    fun parseTransactionAmount() = if (transactionAmount.isNullOrEmpty()) "0.0" else transactionAmount.toCurrency()

    /**
     * Convert tax amount from String to Currency format
     * @return String
     */
    fun parseTaxAmount() = if(reasonId.isNullOrEmpty()) {
        if (taxAmount.isNullOrEmpty()) "0.0" else taxAmount.toCurrency()
    }else{
        if (changedTaxAmount.isNullOrEmpty()) "0.0" else changedTaxAmount.toCurrency()
    }

    fun isPaidByCash() = (payByCash?.equals("Yes", true) == true)

    fun isLocationAvailable() = location?.isNotEmpty()

    fun parseTransactionDate() = transactionDate?.fromServerDateToYYYYMMDD()

    fun getTaxImage() =
            if(reasonId.isNullOrEmpty()) {
                if (taxAmount?.toDoubleOrNull() ?: 0.0 > 0.0) {
                    R.drawable.tax_fill
                } else {
                    R.drawable.tax_unfill
                }
            }else{
                if (changedTaxAmount?.toDoubleOrNull() ?: 0.0 > 0.0) {
                    R.drawable.tax_fill
                } else {
                    R.drawable.tax_unfill
                }
            }

    fun isTaxAvailable()=  if(reasonId.isNullOrEmpty())  taxAmount?.toDoubleOrNull() ?: 0.0 > 0.0 else changedTaxAmount?.toDoubleOrNull() ?: 0.0 > 0.0
}