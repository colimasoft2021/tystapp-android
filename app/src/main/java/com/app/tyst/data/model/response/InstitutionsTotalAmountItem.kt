package com.app.tyst.data.model.response

import android.os.Parcelable
import com.app.tyst.utility.extension.toCurrency
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InstitutionsTotalAmountItem(

        @field:SerializedName("tax_amount")
        val taxAmount: String? = "0.0",

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("transaction_amount")
        val transactionAmount: String? = "0.0",

        @field:SerializedName("institution_name")
        val institutionName: String? = null,

        @field:SerializedName("institution_id")
        val institutionId: String? = null,

        @field:SerializedName("unique_institution_id")
        val uniqueInstitutionId: String? = null,

        @field:SerializedName("error_code")
        val errorCode: String? = "0.0",

        @field:SerializedName("error_message")
        val errorMessage: String? = null,

        @field:SerializedName("login_required")
        val loginRequired: String? = null
) : Parcelable {
    fun getTotalAmount() = if (transactionAmount.isNullOrEmpty()) "0.0" else transactionAmount.toCurrency()

    fun getTotalTaxAmount() = if (taxAmount.isNullOrEmpty()) "0.0" else taxAmount.toCurrency()

    fun isPaidByCash() = institutionId.isNullOrEmpty()

    fun isError() = errorCode?.isNotEmpty() == true

    fun isLoginRequired() = loginRequired?.equals("Yes",true)
}