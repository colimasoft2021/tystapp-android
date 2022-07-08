package com.app.tyst.ui.authentication.login.loginwithemailsocial

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.utility.extension.isValidEmail
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.toEncrypt
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.PASSWORD_EMPTY
import com.app.tyst.utility.validation.createValidationResult
import com.dc.retroapi.utils.WebServiceUtils
import com.google.gson.JsonElement
import com.plaid.linkbase.models.configuration.LinkConfiguration
import com.plaid.linkbase.models.configuration.PlaidProduct
import com.plaid.linkbase.models.connection.LinkConnection
//import com.plaid.linkbase.models.LinkConfiguration
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.PlaidProduct
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity]
 */
class LoginWithEmailSocialViewModel(app: Application) : BaseViewModel(app) {

    var loginResponseMutableLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()
    var socialLoginMutableLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()
    var addAccountLiveData = MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>()
    var sendLinkData = MutableLiveData<WSObserverModel<JsonElement>>()
    /**
     * Validate login inputs
     */
    fun isValid(password: String, email: String): Boolean {
        when {
            email.isEmpty() -> {
                validationObserver.value = createValidationResult(EMAIL_EMPTY, R.id.inputEmail)
                return false
            }

            !email.isValidEmail() -> {
                validationObserver.value = createValidationResult(EMAIL_INVALID, R.id.inputEmail)
                return false
            }
            password.isEmpty() -> {
                validationObserver.value = createValidationResult(PASSWORD_EMPTY, R.id.inputPassword)
                return false
            }
            else -> return true
        }
    }

    /**
     * Api call login
     */

    fun callLogin(email: String = "", password: String = "", socialType: String = "", socialId: String = "", isEncrypted: Boolean = false) {
        val map = HashMap<String, String>()
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = sharedPreference.deviceToken ?: ""
        if (socialType.isNotEmpty()) {
            map["social_login_type"] = socialType
            map["social_login_id"] = socialId
            LoginWithEmailSocialRepository(this@LoginWithEmailSocialViewModel).callLoginWithSocial(map = map,
                    loginMutableLiveData = socialLoginMutableLiveData)
        } else {
            if (isEncrypted)
                map["api_version"] = "1.1"
            map["email"] = email
            map["password"] = if (isEncrypted) password.toEncrypt() else password
            LoginWithEmailSocialRepository(this@LoginWithEmailSocialViewModel).callLoginWithEmail(map = map,
                    loginMutableLiveData = loginResponseMutableLiveData)
        }
    }

    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        sharedPreference.isSkip = false
        sharedPreference.userDetail = loginResponse
        sharedPreference.isLogin = true
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
//        sharedPreference.isAdRemoved = loginResponse?.purchaseStatus.equals("Yes")
    }

    fun saveInstitutionDetail(plaidInstitutionDetail: ArrayList<PlaidInstitutionResponse>?) {
        sharedPreference.setInstitutionsList(plaidInstitutionDetail)
    }

    fun clearUserLoginData() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
    }

    /**
     * This will return link configuration for open Plaid sdk
     * @return LinkConfiguration
     */
    fun getLinkConfiguration() = LinkConfiguration(
            clientName = app.getString(R.string.application_name),
            products = listOf(PlaidProduct.TRANSACTIONS),
            language = Locale.ENGLISH.language,
            countryCodes = listOf(Locale.US.country)
    )

    /**
     * Call api for add user bank account in our server
     * @param linkConnection LinkConnection
     */
    fun callAddBankAccount(linkConnection: LinkConnection) {
        val map = HashMap<String, String>()
        map["institution_id"] = linkConnection.linkConnectionMetadata.institutionId ?: ""
        map["public_token"] = linkConnection.publicToken
        map["institution_name"] = linkConnection.linkConnectionMetadata.institutionName ?: ""
        AddAccountRepository(this).callAddBankAccount(map, addAccountLiveData)
    }

    /**
     * Api call for send verification link
     */

    fun callResendLink(email: String = "") {
        val map = HashMap<String, String>()

        map["email"] = email
        LoginWithEmailSocialRepository(this@LoginWithEmailSocialViewModel).callSendVerificationLink(map = map,
                sendLinkData = sendLinkData)
    }
}
