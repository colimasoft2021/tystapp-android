package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Get public token of an institution for re-authenticate it
 */
data class PublicTokenResponse (

        @field:SerializedName("user_institution_id")
        val userInstitutionId: String? = null,

        @field:SerializedName("public_token")
        val publicToken: String? = null,

        @field:SerializedName("message")
        val message: String? = null
)