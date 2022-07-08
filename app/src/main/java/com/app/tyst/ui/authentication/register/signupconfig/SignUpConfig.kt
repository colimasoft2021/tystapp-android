package com.app.tyst.ui.authentication.register.signupconfig

import com.google.gson.annotations.SerializedName

data class SignUpConfig(

        @field:SerializedName("zip")
        var zip: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("dateofbirth")
        var dateofbirth: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("firstname")
        var firstname: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("city")
        var city: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("phonenumber")
        var phonenumber: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("skip")
        var skip: String = "0",

        @field:SerializedName("streetaddress")
        var streetaddress: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("profilepictureoptional")
        var profilepictureoptional: String = "1",

        @field:SerializedName("state")
        var state: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("email")
        var email: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("lastname")
        var lastname: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("username")
        var username: SignUpConfigItem = SignUpConfigItem()
)