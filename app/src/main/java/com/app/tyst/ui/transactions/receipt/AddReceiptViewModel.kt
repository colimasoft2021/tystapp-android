package com.app.tyst.ui.transactions.receipt

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.AddReceiptRequest
import com.app.tyst.data.model.response.CategoryResponse
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.ui.authentication.register.LocationAddressModel
import com.app.tyst.ui.authentication.register.SignUpRepository
import com.app.tyst.ui.authentication.register.signupconfig.SignUpConfigItem
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.ui.settings.feedback.FeedbackImageModel
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.validation.*
import com.dc.retroapi.utils.AESEncrypter
import com.dc.retroapi.utils.WebServiceUtils
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.ArrayList

/**
 * View model call for performing all business logic of [com.app.tyst.ui.transactions.receipt.AddReceiptActivity]
 */
class AddReceiptViewModel(app: Application) : BaseViewModel(app) {
    var statesLiveData = MutableLiveData<WSObserverModel<ArrayList<StatesResponse>>>()
    private var statesList = ArrayList<StatesResponse>()
    var adReceiptLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var categoryData = MutableLiveData<WSObserverModel<ArrayList<CategoryResponse>>>()
    private var categories = ArrayList<CategoryResponse>()
    var imageList = ArrayList<FeedbackImageModel>()
    private val MAX_IMAGE = 3

    init {
        imageList.add(FeedbackImageModel())
    }

    fun getStatesList() = statesList

    fun getCategoryList() = categories

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

    /**
     * Api call for get states list of US
     */
    fun callGetStateList() {
        SignUpRepository(this).callGetStateList(statesLiveData = statesLiveData)
    }

    /**
     * Api call for get category list
     */
    fun callCategoryList() {
        AddReceiptRepository(this).callGetCategoryList(categoryData = categoryData)
    }

    fun getAddReceiptRequest(receiptImage: String = "", category: String = "", storeName: String = "",
                             location: String = "", city: String = "",
                             stateId: String = "", zipCode: String = "",
                             amount: String = "", transactionDate: String = "",
                             taxAmount: String = "", latitude: String = "",
                             longitude: String = ""): AddReceiptRequest {
        val request = AddReceiptRequest()
        request.category = category
        request.storeName = storeName
        request.location = location
        request.city = city
        request.stateId = stateId
        request.zipCode = zipCode
        request.amount = amount
        request.transactionDate = transactionDate
        request.taxAmount = taxAmount
        request.receiptImage = receiptImage
        request.latitude = latitude
        request.longitude = longitude
        return request
    }

    /**
     * Api call SignUp
     */

    fun callPayByCash(request: AddReceiptRequest) {
        val map = HashMap<String, RequestBody>()
        map["category"] = WebServiceUtils.getStringRequestBody(request.category)
        map["store_name"] = WebServiceUtils.getStringRequestBody(request.storeName)
        map["location"] = WebServiceUtils.getStringRequestBody(request.location)
        map["state_id"] = WebServiceUtils.getStringRequestBody(request.stateId)
        map["zip_code"] = WebServiceUtils.getStringRequestBody(request.zipCode)
        map["amount"] = WebServiceUtils.getStringRequestBody(request.amount)
        map["transaction_date"] = WebServiceUtils.getStringRequestBody(request.transactionDate)
        map["tax_amount"] = WebServiceUtils.getStringRequestBody(request.taxAmount)
        map["latitude"] = WebServiceUtils.getStringRequestBody(request.latitude)
        map["longitude"] = WebServiceUtils.getStringRequestBody(request.longitude)
        map["images_count"] = WebServiceUtils.getStringRequestBody(imageList.size.toString())

        AddReceiptRepository(this@AddReceiptViewModel).callPayByCash(map = map,
                files = getImageFiles(),
                addReceiptData = adReceiptLiveData)

    }

    /**
     * Validate add receipt inputs
     */
    fun isValid(request: AddReceiptRequest): Boolean {
        when {

            //Store Name
            request.storeName.isEmpty() -> {
                validationObserver.value = createValidationResult(EMPTY_STORE_NAME, R.id.inputStoreName)
                return false
            }

            //Place
            request.location.isEmpty() -> {
                validationObserver.value = createValidationResult(ADDRESS_EMPTY, R.id.inputPlace)
                return false
            }

            //City
            !isValidInputText(text = request.city, emptyFailType = CITY_EMPTY, viewId = R.id.inputCity,
                    validationObserver = validationObserver, config = SignUpConfigItem().apply {
                visible = "1"
                optional = "0"
            }) -> {
                return false
            }

            //State
            request.stateId.isEmpty() -> {
                validationObserver.value = createValidationResult(STATE_EMPTY, R.id.inputState)
                return false
            }


            //Zip Code
            !isValidInputText(text = request.zipCode, emptyFailType = ZIP_CODE_EMPTY,
                    invalidFailType = ZIP_CODE_INVALID, viewId = R.id.inputZipCode,
                    minimumLength = app.resources.getInteger(R.integer.zip_code_min_length),
                    maximumLength = app.resources.getInteger(R.integer.zip_code_max_length),
                    validationObserver = validationObserver, config = SignUpConfigItem().apply {
                visible = "1"
                optional = "0"
            }) -> {

                return false
            }


            //Category
            request.category.isEmpty() -> {
                validationObserver.value = createValidationResult(EMPTY_CATEGORY, R.id.inputCategory)
                return false
            }

            //DOB
            !isValidInputText(text = request.transactionDate, emptyFailType = DOB_EMPTY, viewId = R.id.inputTransactionDate,
                    validationObserver = validationObserver, config = SignUpConfigItem().apply {
                visible = "1"
                optional = "0"
            }) -> {
                return false
            }

            //Total Amount
            request.amount.isEmpty() || request.amount.toDoubleOrNull() ?: 0.0 <= 0.0 -> {
                validationObserver.value = createValidationResult(EMPTY_TOTAL_AMOUNT, R.id.inputTotalAmount)
                return false
            }


//            //Tax Amount
//            request.taxAmount.isEmpty() -> {
//                validationObserver.value = createValidationResult(EMPTY_TAX_AMOUNT, R.id.inputTaxApplied)
//                return false
//            }
//
//            ((request.taxAmount.toDoubleOrNull() ?: 0.0) > (request.amount.toDoubleOrNull()
//                    ?: 0.0)) -> {
//                validationObserver.value = createValidationResult(INVALID_TAX_AMOUNT, R.id.inputTaxApplied)
//                return false
//            }

            ((getTaxPercentage(request.taxAmount, request.amount).toDoubleOrNull()?:0.0 > sharedPreference.maxTaxPercentage?.toDoubleOrNull()?:0.0)) -> {
                validationObserver.value = createValidationResult(INVALID_TAX_AMOUNT, R.id.inputTaxApplied)
                return false
            }

            else -> return true
        }
    }

    /**
     * Get MultiPartBody list of selected images
     */
    private fun getImageFiles(): ArrayList<MultipartBody.Part>? {
        val files = ArrayList<MultipartBody.Part>()
        imageList.filter { it.contentUri != null }.forEachIndexed { index, FeedbackImageModel ->
            files.add(WebServiceUtils.getStringMultipartBodyPart("receipt_image_" + (index + 1), FeedbackImageModel.contentUri?.toString()
                    ?: ""))
        }
        return if (files.isEmpty()) null else files
    }

    /**
     * Return list of selected images by user
     * @param imageModel selected image
     */
    fun getSelectedImage(imageModel: FeedbackImageModel): ArrayList<FeedbackImageModel> {
        // if user added 5th image then remove add image option.
        return imageList.apply {
            if (size == MAX_IMAGE) {
                removeAt(0)
            }
            add(imageModel)
        }
    }

    /**
     * Return list of images after removed selected image
     * @param position position of selected image, which need to be removed.
     */
    fun getImagesAfterRemove(position: Int): ArrayList<FeedbackImageModel> {
        return imageList.apply {
            removeAt(position)
            // if user added 5 option and remove any one, then show add image option.
            if (find { it.contentUri == null } == null) {
                add(0, FeedbackImageModel())
            }
        }
    }

    fun getTaxPercentage(taxAmount: String, totalAmount: String): String {
        return (taxAmount.toDoubleOrNull()
                ?: 0.0).whatPercentageOf(totalAmount.toDoubleOrNull()
                ?: 0.0).uptoTwoDecimal()
    }
}