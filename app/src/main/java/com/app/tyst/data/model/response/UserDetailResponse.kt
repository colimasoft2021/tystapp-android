package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * This class will hold user's account and plaid institution data
 * @property getUserDetails List<GetUserDetailsItem?>?
 * @property getInstitutions List<GetInstitutionsItem?>?
 * @constructor
 */
data class UserDetailResponse(

        @field:SerializedName("get_user_details")
        val getUserDetails: ArrayList<LoginResponse> = ArrayList(),

        @field:SerializedName("get_institutions")
        val getInstitutions: ArrayList<PlaidInstitutionResponse> = ArrayList()
)