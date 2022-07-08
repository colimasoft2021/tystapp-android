package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * This data class is used for parse all ransaction data of an account
 */
data class AccountTransactionsDataResponse(
        @field:SerializedName("transactions_data")
        var transactionsData: ArrayList<TransactionDataResponse> = ArrayList(),

        @field:SerializedName("total_amount")
        var totalAmount: ArrayList<TotalAmountItem> = ArrayList()
)