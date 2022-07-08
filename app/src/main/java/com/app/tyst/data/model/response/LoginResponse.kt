package com.app.tyst.data.model.response

import android.telephony.PhoneNumberUtils
import com.app.tyst.utility.extension.fromServerDateToYYYYMMDD
import com.google.gson.annotations.SerializedName

data class LoginResponse(

        @field:SerializedName("address")
        val address: String? = null,

        @field:SerializedName("email_verified")
        val emailVerified: String? = null,

        @field:SerializedName("device_model")
        val deviceModel: String? = null,

        @field:SerializedName("city")
        val city: String? = null,

        @field:SerializedName("user_name")
        val userName: String? = null,

        @field:SerializedName("latitude")
        val latitude: String? = null,

        @field:SerializedName("mobile_no")
        var mobileNo: String? = null,

        @field:SerializedName("last_name")
        val lastName: String? = null,

        @field:SerializedName("device_type")
        val deviceType: String? = null,

        @field:SerializedName("device_os")
        val deviceOs: String? = null,

        @field:SerializedName("zip_code")
        val zipCode: String? = null,

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("added_at")
        val addedAt: String? = null,

        @field:SerializedName("profile_image")
        val profileImage: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("user_id")
        val userId: String? = null,

        @field:SerializedName("dob")
        val dob: String? = null,

        @field:SerializedName("device_token")
        val deviceToken: String? = null,

        @field:SerializedName("state_id")
        val stateId: String? = null,

        @field:SerializedName("first_name")
        val firstName: String? = null,

        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("longitude")
        val longitude: String? = null,

        @field:SerializedName("status")
        val status: String? = null,

        @field:SerializedName("social_login_type")
        val loginType: String? = "",

        @field:SerializedName("social_login_id")
        val socialLoginId: String? = "",

        @field:SerializedName("purchase_status")
        var purchaseStatus: String? = "",  //Yes/No

        @field:SerializedName("purchase_receipt_data")
        val purchaseReceiptData: String? = "",

        @field:SerializedName("push_notify")
        var notification: String? = ""
) {
    fun getFullName(): String {
        return firstName + " " + lastName
    }

    fun isSocialLogin(): Boolean {
        return !socialLoginId.isNullOrEmpty()
    }

    fun isNotificationOn(): Boolean {
        return notification.equals("Yes", true)
    }

    fun getDOBStr(): String {
        return dob.fromServerDateToYYYYMMDD()
    }

    fun getFormatedPhoneNumber(): String {
        return if (mobileNo.isNullOrEmpty()) "" else PhoneNumberUtils.formatNumber(mobileNo, "US")
    }

    fun isEmailEmpty() = email.isNullOrEmpty()

    fun isPhoneNumberEmpty() = mobileNo.isNullOrEmpty()

    fun isSubscripbed():Boolean{
        return purchaseStatus.equals("Yes",true)
    }

}