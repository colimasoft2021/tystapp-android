package com.app.tyst.ui.settings.editprofile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.data.model.response.LoginResponse
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.data.model.response.UserDetailResponse
import com.app.tyst.ui.authentication.register.LocationAddressModel
import com.app.tyst.ui.authentication.register.signupconfig.SignUpConfig
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.isValidInputText
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.toMMDDYYYStr
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.*
import com.dc.retroapi.utils.WebServiceUtils
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.editprofile.EditProfileActivity]
 */
class EditProfileViewModel(app: Application) : BaseViewModel(app) {
    var statesLiveData = MutableLiveData<WSObserverModel<ArrayList<StatesResponse>>>()
    var editLiveData = MutableLiveData<WSObserverModel<UserDetailResponse>>()

    private var statesList = ArrayList<StatesResponse>()


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

    fun getEditProfileRequest(userProfileImage: String = "", userName: String = "", firstName: String = "",
                              lastName: String = "", dob: String = "", phoneNumber: String = "",
                              address: String = "", latitude: String = "",
                              longitude: String = "", city: String = "",
                              state: String = "", stateId: String = "", zip: String = ""
    ): SignUpRequestModel {
        val request = SignUpRequestModel()
        request.profileImage = userProfileImage
        request.userName = userName
        request.firstName = firstName
        request.lastName = lastName
        request.mobileNumber = phoneNumber
        request.dob = dob
        request.address = address
        request.latitude = latitude
        request.longitude = longitude
        request.city = city
        request.state = state
        request.stateId = stateId
        request.zipCode = zip
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
            else -> return true
        }
    }

    /**
     * Api call for get states list of US
     */
    fun callGetStateList() {
        EditProfileRepository(this).callGetStateList(statesLiveData = statesLiveData)
    }

    /**
     * Api call for edit profile
     */

    fun callEditProfile(request: SignUpRequestModel) {
        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody(request.userName)
        map["first_name"] = WebServiceUtils.getStringRequestBody(request.firstName)
        map["last_name"] = WebServiceUtils.getStringRequestBody(request.lastName)
        map["dob"] = WebServiceUtils.getStringRequestBody(request.dob)
        map["address"] = WebServiceUtils.getStringRequestBody(request.address)
        map["city"] = WebServiceUtils.getStringRequestBody(request.city)
        map["latitude"] = WebServiceUtils.getStringRequestBody(request.latitude)
        map["longitude"] = WebServiceUtils.getStringRequestBody(request.longitude)
        map["state_id"] = WebServiceUtils.getStringRequestBody(request.stateId)
        map["zipcode"] = WebServiceUtils.getStringRequestBody(request.zipCode)
        map["mobile_number"] = WebServiceUtils.getStringRequestBody(request.mobileNumber)
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(sharedPreference.deviceToken
                ?: "")
        EditProfileRepository(this@EditProfileViewModel).callEditProfile(map = map,
                file = if (request.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart("user_profile", request.profileImage),
                editLiveData = editLiveData)
    }

    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        sharedPreference.isSkip = false
        sharedPreference.userDetail = loginResponse
        sharedPreference.isLogin = true
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
        (app as MainApplication).isProfileUpdated.value = true
//        sharedPreference.isAdRemoved = loginResponse?.purchaseStatus.equals("Yes")
    }

    fun saveInstitutionDetail(plaidInstitutionDetail:ArrayList<PlaidInstitutionResponse>?){
        sharedPreference.setInstitutionsList(plaidInstitutionDetail)
    }
}