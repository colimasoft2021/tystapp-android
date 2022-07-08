package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class InstitutionsTransactionResponse(

        @field:SerializedName("get_institutions_total_amount")
        var institutionsTotalAmount: ArrayList<InstitutionsTotalAmountItem> = ArrayList(),

        @field:SerializedName("total_amount")
        var totalAmount: ArrayList<TotalAmountItem> = ArrayList()
)