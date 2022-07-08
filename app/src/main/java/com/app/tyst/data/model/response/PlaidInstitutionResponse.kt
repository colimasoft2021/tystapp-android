package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * This class is used for save user's plaid institution data after add user's bank detail
 * @property accessToken String?
 * @property addedAt String?
 * @property publicToken String?
 * @property isFirstTransactionsDumped String?
 * @property logo String?
 * @property institutionName String?
 * @property institutionId String?
 * @property status String?
 * @constructor
 */
data class PlaidInstitutionResponse(

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("added_at")
        val addedAt: String? = null,

        @field:SerializedName("public_token")
        val publicToken: String? = null,

        @field:SerializedName("is_first_transactions_dumped")
        var isFirstTransactionsDumped: String? = null,

        @field:SerializedName("institution_logo")
        val institutionLogo: String? = null,

        @field:SerializedName("institution_name")
        val institutionName: String? = null,

        @field:SerializedName("institution_id")
        val institutionId: String? = null,

        @field:SerializedName("status")
        val status: String? = null,

        @field:SerializedName("institution_primary_color")
        val institutionPrimaryColor: String? = null,

        @field:SerializedName("institution_url")
        val institutionUrl: String? = null,

        @field:SerializedName("unique_institution_id")
        val uniqueInstitutionId: String? = null,

        @field:SerializedName("error_code")
        val errorCode: String? = "0.0",

        @field:SerializedName("error_message")
        val errorMessage: String? = null,

        @field:SerializedName("login_required")
        val loginRequired: String? = null
)