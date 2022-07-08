package com.dc.mvvmskeleton.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by HB on 20/8/19.
 */
class User {

    @SerializedName("user_code")
    @Expose
    var userCode: String? = null
    @SerializedName("display_name")
    @Expose
    var displayName: String? = null
    @SerializedName("gender")
    @Expose
    var gender: String? = null
    @SerializedName("profile_image")
    @Expose
    var profileImage: String? = null
    @SerializedName("user_status")
    @Expose
    var userStatus: String? = null
    @SerializedName("is_verified")
    @Expose
    var isVerified: String? = null
    @SerializedName("chat_user_name")
    @Expose
    var chatUserName: String? = null
    @SerializedName("user_id")
    @Expose
    var userId: String? = null
    @SerializedName("country_code")
    @Expose
    var countryCode: String? = null
    @SerializedName("mobile_no")
    @Expose
    var mobileNo: String? = null
    @SerializedName("email_address")
    @Expose
    var emailAddress: String? = null
    @SerializedName("referral_code")
    @Expose
    var referralCode: String? = null
    @SerializedName("user_login_id")
    @Expose
    private val userLoginId: String? = null
}
