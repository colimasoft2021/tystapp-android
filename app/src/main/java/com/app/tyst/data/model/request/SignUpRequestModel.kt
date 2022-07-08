package com.app.tyst.data.model.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignUpRequestModel(
        var profileImage: String = "",
        var userName: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var mobileNumber: String = "",
        var email: String = "",
        var dob:String="",
        var address:String="",
        var city:String="",
        var state:String="",
        var stateId: String = "",
        var latitude: String = "",
        var longitude: String = "",
        var zipCode:String="",
        var password: String = "",
        var confirmPassword: String = "",
        var socialType: String = "",
        var socialId: String = "",
        var tnc:Boolean = true
) : Parcelable
