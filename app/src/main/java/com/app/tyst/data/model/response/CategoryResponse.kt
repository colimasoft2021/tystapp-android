package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class CategoryResponse (

        @field:SerializedName("category")
        val category: String? = null,

        @field:SerializedName("category_id")
        val category_id: String? = null
){
    override fun toString(): String {
        return category?:""
    }
}