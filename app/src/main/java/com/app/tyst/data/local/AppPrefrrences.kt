/*
 * Copyright (c) 2015. Configure.IT, All Rights Reserved.
 */

package com.app.tyst.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.app.tyst.R
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.data.model.response.PlaidSettingResponse
import com.app.tyst.utility.IConstants
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * This class is used for storing and retrieving shared preference values.
 */

class AppPrefrrences(context: Context) {

    private val gson = GsonBuilder().create()

    private fun <T> getObjectFromJsonString(jsonData: String, modelClass: Class<*>): Any? {
        return gson.fromJson(jsonData, modelClass)
    }

    private fun getJsonStringFromObject(modelClass: Any): String {
        return gson.toJson(modelClass)
    }


    //    val pref = context.getSharedPreferences(context.getString(R.string.application_name), Context.MODE_PRIVATE)
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    val pref = EncryptedSharedPreferences
            .create(
                    context.getString(R.string.application_name),
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

    var isLogin: Boolean
        get() = pref.getBoolean("isLogin", false)
        set(value) = pref.edit().putBoolean("isLogin", value).apply()

    var deviceToken: String?
        get() = pref.getString("deviceToken", "")
        set(value) = pref.edit().putString("deviceToken", value).apply()

    var authToken: String?
        get() = pref.getString("authToken", "")
        set(value) = pref.edit().putString("authToken", value).apply()

    var isSkip: Boolean
        get() = pref.getBoolean("isSkip", false)
        set(value) = pref.edit().putBoolean("isSkip", value).apply()

    var isTutorialShowed: Boolean
        get() = pref.getBoolean("isTutorialShow", false)
        set(value) = pref.edit().putBoolean("isTutorialShow", value).apply()

//    var isAdRemoved: Boolean
//        get() = pref.getBoolean("isAdRemoved", false)
//        set(value) = pref.edit().putBoolean("isAdRemoved", value).apply()

    var userDetail: LoginResponse?
        get() = getObjectFromJsonString<LoginResponse>(pref.getString("userDetail", "")
                ?: "", LoginResponse::class.java) as LoginResponse?
        set(value) =
            if (value == null) {
                pref.edit().putString("userDetail", "").apply()
            } else {
                pref.edit().putString("userDetail", getJsonStringFromObject(value)).apply()
            }

    var notificationDefaultChannelSet: Boolean
        get() = pref.getBoolean(IConstants.SP_NOTIFICATION_CHANNEL_DEFAULT, false)
        set(value) = pref.edit().putBoolean(IConstants.SP_NOTIFICATION_CHANNEL_DEFAULT, value).apply()

    /**
     * Store application login type i.e. login with email, phone, email+social and phone+social
     */
    var appLoginType: String?
        get() = pref.getString("loginType", "")
        set(value) = pref.edit().putString("loginType", value).apply()

    fun getInstitutionsList(): ArrayList<PlaidInstitutionResponse>? {

        val groupListType = object : TypeToken<ArrayList<PlaidInstitutionResponse>>() {
        }.type
        return gson.fromJson(pref.getString("institutionDetail", ""), groupListType)
    }

    fun setInstitutionsList(value: ArrayList<PlaidInstitutionResponse>?) {
        if (value.isNullOrEmpty()) {
            pref.edit().putString("institutionDetail", "").apply()
        } else {
            pref.edit().putString("institutionDetail", getJsonStringFromObject(value)).apply()
        }
    }

    var plaidSettings: PlaidSettingResponse?
        get() = getObjectFromJsonString<PlaidSettingResponse>(pref.getString("plaid_settings", "")
                ?: "", PlaidSettingResponse::class.java) as PlaidSettingResponse?
        set(value) =
            if (value == null) {
                pref.edit().putString("plaid_settings", "").apply()
            } else {
                pref.edit().putString("plaid_settings", getJsonStringFromObject(value)).apply()
            }

    var maxTaxPercentage: String?
        get() = pref.getString("maxTaxPercentage", "")
        set(value) = pref.edit().putString("maxTaxPercentage", value).apply()

    var maxTipPercentage: String?
        get() = pref.getString("maxTipPercentage", "")
        set(value) = pref.edit().putString("maxTipPercentage", value).apply()
}