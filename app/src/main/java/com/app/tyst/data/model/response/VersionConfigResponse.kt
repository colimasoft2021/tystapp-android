package com.app.tyst.data.model.response

import android.content.Context
import android.os.Parcelable
import com.app.tyst.utility.extension.getAppVersion
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * This data class is used for application version checking
 * @property versionUpdateOptional String? 1 -> Application update is mandatory and 0 -> Application update is optional
 * @property iphoneVersionNumber String?
 * @property androidVersionNumber String? Latest android application version
 * @property versionUpdateCheck String? Need to check app version or not. 1 -> Need to check and 0 -> No need to check
 * @property versionCheckMessage Message which need to show
 * @constructor
 */
@Parcelize
data class VersionConfigResponse(

        @field:SerializedName("version_update_optional")
        val versionUpdateOptional: String? = null,

        @field:SerializedName("iphone_version_number")
        val iphoneVersionNumber: String? = null,

        @field:SerializedName("android_version_number")
        val androidVersionNumber: String? = null,

        @field:SerializedName("version_update_check")
        val versionUpdateCheck: String? = null,

        @field:SerializedName("version_check_message")
        val versionCheckMessage: String? = null,
        @field:SerializedName("maximum_tax_perc")
        val maximumTaxPerc: String? = null,
        @field:SerializedName("maximum_tip_perc")
        val maximumTipPerc: String? = null,

        @field:SerializedName("terms_conditions_updated")
        var termsConditionsUpdated: String? = null,

        @field:SerializedName("privacy_policy_updated")
        var privacyPolicyUpdated: String? = null
) : Parcelable {
    fun shouldShowVersionDialog(context: Context) =
            (((androidVersionNumber?.compareTo(getAppVersion(context, true))
                    ?: 0) > 0) && versionUpdateCheck.equals("1"))

    fun isOptionalUpdate() = versionUpdateOptional.equals("1")

    fun shouldShowTNCUpdated() = termsConditionsUpdated.equals("1")

    fun shouldShowPrivacyPolicyUpdated() = privacyPolicyUpdated.equals("1")
}