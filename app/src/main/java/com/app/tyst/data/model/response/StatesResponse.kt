package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class StatesResponse(

        @field:SerializedName("country_code")
        val countryCode: String? = null,

        @field:SerializedName("state")
        val state: String? = null,

        @field:SerializedName("state_id")
        val stateId: String? = null,

        @field:SerializedName("state_code")
        val stateCode: String? = null,

        @field:SerializedName("status")
        val status: String? = null
){
        override fun toString(): String {
                return state?:""
        }
}