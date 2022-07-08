package com.app.tyst.ui.authentication.register

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.data.model.response.*
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.authentication.register.signupconfig.SignUpConfig
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.PasswordStrength
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.*
import com.dc.retroapi.utils.WebServiceUtils.getStringMultipartBodyPart
import com.dc.retroapi.utils.WebServiceUtils.getStringRequestBody
//import com.plaid.linkbase.models.LinkConfiguration
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.PlaidProduct
import com.plaid.linkbase.models.connection.LinkConnection
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.signup.SignUpActivity]
 */
class SignUpViewModel(app: Application) : BaseViewModel(app) {

    var signUpLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()
    var otpLiveData = MutableLiveData<WSObserverModel<ArrayList<OTPResponse>>>()
    var statesLiveData = MutableLiveData<WSObserverModel<ArrayList<StatesResponse>>>()
    private var statesList = ArrayList<StatesResponse>()
    var addAccountLiveData = MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>()

    private val signUpConfiguration: SignUpConfig by lazy {
        AppConfig.getSignUpConfiguration()
    }

    fun getStatesList() = statesList

    /**
     * Return index of state from state list
     * @param state String Searched state name or state id, whose index in array list need to find
     * @return Int
     */
    fun getStateIndex(state: String) = statesList.indexOfFirst { it.state?.equals(state.trim(), ignoreCase = true) == true || it.stateCode?.equals(state.trim(), ignoreCase = true) == true }


    /**
     * Parse address got from places api into city, state etc...
     * Here we consider address format  " <Address>, <City>, <State ZipCode>, <Country>"
     * @param placeAddress String Place full address
     * @return LocationAddressModel Parsed Address
     *
     * Example: 7102 Grand Horizons Blvd, Orlando, FL 32821, USA
     */
    fun getParseAddress(placeAddress: String?): LocationAddressModel {
        val splitAddress = placeAddress?.split(",")?.reversed()
        return LocationAddressModel().apply {
            fullAddress = placeAddress ?: ""
            splitAddress?.forEachIndexed { index, str ->
                if (index == 0) country = str.trim()
                if (index == 1) {
                    val stateSplit = str.trim().split(" ")
                    if (stateSplit.isNotEmpty()) state = stateSplit[0].trim()
                    if (stateSplit.size > 1) zipCode = stateSplit[1].trim()
                }
                if (index == 2) city = str.trim()
                if (index == 3) address = str.trim()
            }
        }
    }


    /**
     * Convert year, month and day into readable date
     * @param year Int
     * @param month Int
     * @param dayOfMonth Int
     * @return String
     */
    fun getDateFromPicker(year: Int, month: Int, dayOfMonth: Int): String {
        val calender = Calendar.getInstance()
        calender.set(Calendar.YEAR, year)
        calender.set(Calendar.MONTH, month)
        calender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calender.time.toMMDDYYYStr()
    }

    fun getSignUpRequest(userProfileImage: String = "", userName: String = "", firstName: String = "",
                         lastName: String = "", email: String = "",
                         phone: String = "", dob: String = "",
                         address: String = "", latitude: String = "",
                         longitude: String = "", city: String = "",
                         state: String = "", stateId: String = "", zip: String = "",
                         password: String = "", confirmPassword: String = "",
                         socialType: String = "", socialId: String = "",
                         tnc: Boolean = true): SignUpRequestModel {
        val request = SignUpRequestModel()
        request.profileImage = userProfileImage
        request.userName = userName
        request.firstName = firstName
        request.lastName = lastName
        request.email = email
        request.mobileNumber = phone
        request.dob = dob
        request.address = address
        request.latitude = latitude
        request.longitude = longitude
        request.city = city
        request.state = state
        request.stateId = stateId
        request.zipCode = zip
        request.password = password
        request.confirmPassword = confirmPassword
        request.socialType = socialType
        request.socialId = socialId
        request.tnc = tnc
        return request
    }

    /**
     * Validate sign up inputs
     */
    fun isValid(request: SignUpRequestModel): Boolean {
        when {

            //User Name
            !isValidInputText(text = request.userName, emptyFailType = USER_NAME_EMPTY,
                    invalidFailType = USER_NAME_INVALID, viewId = R.id.inputUserName,
                    minimumLength = app.resources.getInteger(R.integer.user_name_min_length),
                    maximumLength = app.resources.getInteger(R.integer.user_name_max_length),
                    validationObserver = validationObserver, config = signUpConfiguration.username) -> {

                return false
            }


            //First Name
            !isValidInputText(text = request.firstName, emptyFailType = FIRST_NAME_EMPTY,
                    invalidFailType = FIRST_NAME_INVALID, viewId = R.id.inputFirstName,
                    minimumLength = app.resources.getInteger(R.integer.first_name_min_length),
                    maximumLength = app.resources.getInteger(R.integer.first_name_max_length),
                    validationObserver = validationObserver, config = signUpConfiguration.firstname) -> {

                return false
            }

            //Last Name
            !isValidInputText(text = request.lastName, emptyFailType = LAST_NAME_EMPTY,
                    invalidFailType = LAST_NAME_INVALID, viewId = R.id.inputLastName,
                    minimumLength = app.resources.getInteger(R.integer.first_name_min_length),
                    maximumLength = app.resources.getInteger(R.integer.first_name_max_length),
                    validationObserver = validationObserver, config = signUpConfiguration.lastname) -> {

                return false
            }


            //Email
            !isValidInputEmail(email = request.email,
                    validationObserver = validationObserver) -> {
                return false
            }

            //Phone number
            !isValidInputPhone(phoneNumber = request.mobileNumber,
                    validationObserver = validationObserver, config = signUpConfiguration.phonenumber) -> {
                return false
            }

            //DOB
            !isValidInputText(text = request.dob, emptyFailType = DOB_EMPTY, viewId = R.id.inputDOB,
                    validationObserver = validationObserver, config = signUpConfiguration.dateofbirth) -> {
                return false
            }

            //Street Address
            !isValidInputText(text = request.address, emptyFailType = ADDRESS_EMPTY, viewId = R.id.inputAddress,
                    validationObserver = validationObserver, config = signUpConfiguration.streetaddress) -> {
                return false
            }

            //City
            !isValidInputText(text = request.city, emptyFailType = CITY_EMPTY, viewId = R.id.inputCity,
                    validationObserver = validationObserver, config = signUpConfiguration.city) -> {
                return false
            }

            //State
            !isValidInputText(text = request.state, emptyFailType = STATE_EMPTY, viewId = R.id.inputState,
                    validationObserver = validationObserver, config = signUpConfiguration.state) -> {
                return false
            }

            //Zip Code
            !isValidInputText(text = request.zipCode, emptyFailType = ZIP_CODE_EMPTY,
                    invalidFailType = ZIP_CODE_INVALID, viewId = R.id.inputZipCode,
                    minimumLength = app.resources.getInteger(R.integer.zip_code_min_length),
                    maximumLength = app.resources.getInteger(R.integer.zip_code_max_length),
                    validationObserver = validationObserver, config = signUpConfiguration.zip) -> {

                return false
            }

            request.socialType.isEmpty() && request.password.isEmpty() -> {
                validationObserver.value = createValidationResult(PASSWORD_EMPTY, R.id.inputPassword)
                return false
            }
            request.socialType.isEmpty() && PasswordStrength.calculateStrength(request.password).value < PasswordStrength.STRONG.value -> {
                validationObserver.value = createValidationResult(PASSWORD_INVALID, R.id.inputPassword)
                return false
            }
            request.socialType.isEmpty() && request.confirmPassword.isEmpty() -> {
                validationObserver.value = createValidationResult(CONFORM_PASSWORD_EMPTY, R.id.inputConfirmPassword)
                return false
            }
            request.socialType.isEmpty() && request.password != request.confirmPassword -> {
                validationObserver.value = createValidationResult(PASSWORD_NOT_MATCH, R.id.inputConfirmPassword)
                return false
            }

            !request.tnc -> {
                validationObserver.value = createValidationResult(TNC_NOT_ACCEPTED, R.id.tvTNC)
                return false
            }

            else -> return true
        }
    }


    /**
     * Api call for check unique email or phone number.
     * @param type String phone/email  phone -> If phone number unique check, email -> If email unique check
     * @param email String user's email which need to check
     * @param phone String user's phone which need to check
     */

    private fun callCheckUnique(type: String, email: String = "", phone: String = "", userName: String = "") {
        val map = HashMap<String, String>()
        map["type"] = type // phone/email
        map["email"] = email
        map["mobile_number"] = phone
        map["user_name"] = userName

        SignUpRepository(this@SignUpViewModel).callCheckUnique(map = map,
                otpLiveData = otpLiveData)
    }

    /**
     * Api call for get states list of US
     */
    fun callGetStateList() {
        SignUpRepository(this).callGetStateList(statesLiveData = statesLiveData)
    }

    /**
     * Api call SignUp
     */

    fun callSignUp(request: SignUpRequestModel,isEncrypted:Boolean = false) {
        val map = HashMap<String, RequestBody>()
        map["user_name"] = getStringRequestBody(request.userName)
        map["first_name"] = getStringRequestBody(request.firstName)
        map["last_name"] = getStringRequestBody(request.lastName)
        map["email"] = getStringRequestBody(request.email)
        map["mobile_number"] = getStringRequestBody(request.mobileNumber)
        map["dob"] = getStringRequestBody(request.dob)
        map["address"] = getStringRequestBody(request.address)
        map["city"] = getStringRequestBody(request.city)
        map["latitude"] = getStringRequestBody(request.latitude)
        map["longitude"] = getStringRequestBody(request.longitude)
        map["state_id"] = getStringRequestBody(request.stateId)
        map["zipcode"] = getStringRequestBody(request.zipCode)
        map["device_type"] = getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = getStringRequestBody(sharedPreference.deviceToken ?: "")

        if (((app as MainApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL) ||
                (app.getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL_SOCIAL)) {
            if (request.socialType.isEmpty()) {
                if(isEncrypted)
                    map["api_version"] =  getStringRequestBody("1.1")
                map["password"] =  if(isEncrypted) getStringRequestBody(request.password.toEncrypt()) else getStringRequestBody(request.password)
                SignUpRepository(this@SignUpViewModel).callSignUpWithEmail(map = map,
                        file = if (request.profileImage.isEmpty()) null else getStringMultipartBodyPart("user_profile", request.profileImage),
                        signUpLiveData = signUpLiveData)
            } else {
                map["social_login_type"] = getStringRequestBody(request.socialType)
                map["social_login_id"] = getStringRequestBody(request.socialId)
                SignUpRepository(this@SignUpViewModel).callSignUpWithSocial(map = map,
                        file = if (request.profileImage.isEmpty()) null else getStringMultipartBodyPart("user_profile", request.profileImage),
                        signUpLiveData = signUpLiveData)
            }


        } else if ((app.getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE) ||
                (app.getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE_SOCIAL)) {
            callCheckUnique(type = "phone", email = request.email,
                    phone = request.mobileNumber, userName = request.userName)
        }


    }

    /**
     * This will return link configuration for open Plaid sdk
     * @return LinkConfiguration
     */
    /*fun getLinkConfiguration() = LinkConfiguration(
            clientName = app.getString(R.string.application_name),
            products = listOf(PlaidProduct.TRANSACTIONS),
            language = Locale.ENGLISH.language,
            countryCodes = listOf(Locale.US.country)
    )*/

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

    /**
     * Call api for add user bank account in our server
     * @param linkConnection LinkConnection
     */
    fun callAddBankAccount(linkConnection: LinkConnection) {
        val map = HashMap<String, String>()
        map["institution_id"] = linkConnection.linkConnectionMetadata.institutionId?:""
        map["public_token"] = linkConnection.publicToken
        map["institution_name"] = linkConnection.linkConnectionMetadata.institutionName?:""
        AddAccountRepository(this).callAddBankAccount(map, addAccountLiveData)
    }

    fun saveInstitutionDetail(plaidInstitutionDetail: ArrayList<PlaidInstitutionResponse>?) {
        sharedPreference.setInstitutionsList(plaidInstitutionDetail)
    }

    fun clearUserLoginData() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
    }

//    private fun getStringMultipartBodyPart(key: String, filePath: String): MultipartBody.Part {
//        val mediaType = "*/*".toMediaType()
//        val file = File(filePath)
//        //val filebody = RequestBody.create(MediaType.parse("*/*"), file)
//        //val filebody = RequestBody.create(mediaType, file)
//        //return MultipartBody.Part.createFormData(key, file.name, filebody)
//        return MultipartBody.Part.createFormData(key, file.name, file.asRequestBody(mediaType))
//    }
}