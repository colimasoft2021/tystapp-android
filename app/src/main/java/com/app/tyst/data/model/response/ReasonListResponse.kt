package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class ReasonListResponse(

        @field:SerializedName("reason")
        val reason: String? = null,

        @field:SerializedName("added_at")
        val addedAt: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("reason_id")
        val reasonId: String? = null,

        @field:SerializedName("status")
        val status: String? = null
){
        override fun toString(): String {
                return reason?:""
        }
}