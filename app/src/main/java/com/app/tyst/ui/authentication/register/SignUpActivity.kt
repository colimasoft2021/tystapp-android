package com.app.tyst.ui.authentication.register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.Social
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.databinding.ActivitySignUpBinding
import com.app.tyst.ui.authentication.otp.otpsignup.OTPSignUpActivity
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.imagecropper.CropperActivity
import com.app.tyst.ui.settings.staticpages.StaticPagesActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.utility.helper.mediahelper.MediaHelper
import com.app.tyst.utility.validation.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.master.permissionhelper.PermissionHelper
import com.plaid.link.Plaid
//import com.plaid.linkbase.models.LinkCancellation
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.LinkEventListener
//import com.plaid.linkbase.models.PlaidApiError
import java.util.*
import kotlin.collections.ArrayList


class SignUpActivity : BaseActivity() {

    companion object {
        /**
         * Start intent to open signup activity with social information
         * @param mContext Context
         * @param social Social
         * @return Intent
         */
        fun getStartIntent(mContext: Context, social: Social): Intent {
            return Intent(mContext, SignUpActivity::class.java).apply {
                putExtra("social", social)
            }
        }
    }

    lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel
        get() = ViewModelProviders.of(this).get(SignUpViewModel::class.java)


    private var social: Social? = null

    private var permissionHelper: PermissionHelper? = null
    private var captureUri: Uri? = null
    private var mediaHelper: MediaHelper? = null
    private var stateSelected = false // Stop to set State Spinner value of spinner initialization
    private var spinnerClick = false

    private lateinit var signUpRequest: SignUpRequestModel
    private var selectedState: StatesResponse? = null
    private var selectedPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SignUpActivity, R.layout.activity_sign_up)
        social = intent.getParcelableExtra("social")
        initView()
    }

    private fun initView() {
        binding.config = AppConfig.getSignUpConfiguration()
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        binding.tvTNC.text = String.format(getString(R.string.agree_terms_and_condition), getString(R.string.application_name))
        initListeners()
        setSocialInformation()
        addObserver()
        setBoldAndColorSpannable(binding.tvTNC, getString(R.string.terms_n_conditions), getString(R.string.privacy_policy))

//        if ((application as MainApplication).stateList.isEmpty()) {
//            mIdlingResource?.setIdleState(false)
//            showHideProgressDialog(true)
//            viewModel.callGetStateList()
//        } else {
//            initSpinners((application as MainApplication).stateList)
//        }
    }

    private fun initSpinners(states: ArrayList<StatesResponse>) {
        viewModel.getStatesList().clear()
        viewModel.getStatesList().addAll(states)
        binding.apply {
            spState.adapter = ArrayAdapter<StatesResponse>(this@SignUpActivity, R.layout.item_spinner,
                    android.R.id.text1,
                    states)
            spState.onItemSelectedListener = statesItemListener
            if (spinnerClick)
                spState.performClick()
            spinnerClick = false
        }
    }

    private fun setSpinners(state: String) {
        binding.spState.setSelection(viewModel.getStateIndex(state))
    }

    /**
     * Item listener for select state
     */
    private val statesItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (stateSelected) {
                binding.inputState.setText(viewModel.getStatesList()[position].state)
                selectedState = viewModel.getStatesList()[position]
                mIdlingResource?.setIdleState(true)
            } else {
                stateSelected = true
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { onBackPressed() }
            btnCreate.makeBounceable()
            btnCreate.clickWithDebounce {
                binding.root.hideKeyBoard()
                if (isNetworkConnected())
                    performSignUp()
            }
            inputState.clickWithDebounce {
                mIdlingResource?.setIdleState(false)
                if (viewModel.getStatesList().isEmpty()) {
                    spinnerClick = true
                    showHideProgressDialog(true)
                    mIdlingResource?.setIdleState(false)
                    viewModel.callGetStateList()
                } else {
                    spState.performClick()
                }
            }
            inputDOB.clickWithDebounce { setDOB() }
            inputAddress.clickWithDebounce {
                openPlacePicker()
            }

            flProfile.clickWithDebounce {
                binding.root.hideKeyBoard()
                DialogUtil.confirmDialog(context = this@SignUpActivity, title = "", msg = getString(R.string.msg_capture_image),
                        positiveBtnText = getString(R.string.camera), negativeBtnText = getString(R.string.gallery), il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        checkAndRequestPermissionCamera()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        checkAndRequestPermissionGallery(false)
                    }
                })
            }
        }
    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@SignUpActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            if (it.failType != null) {
                focusInvalidInput(it.failType!!)
            }
        })

        viewModel.signUpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                if (social == null) {
                    it.settings?.message?.showSnackBar(this@SignUpActivity,
                            IConstants.SNAKBAR_TYPE_SUCCESS,
                            duration = IConstants.SNAKE_BAR_SHOW_TIME)
                    Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
                } else {
                    // If user signup with social, then navigate to home screen
                    viewModel.saveUserDetails(it?.data?.getUserDetails?.get(0))
//                    if (it.data?.getInstitutions.isNullOrEmpty()) {
//                        openPlaidSdk()
//                    } else {
//                        viewModel.saveInstitutionDetail(it.data?.getInstitutions)
                    mIdlingResource?.setIdleState(true)
                    navigateToHomeScreen()
//                    }
                }
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@SignUpActivity)
            }

            mIdlingResource?.setIdleState(true)
        })

        viewModel.otpLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && !it.data.isNullOrEmpty()) {
                navigateToPhoneVerificationScreen(it.data?.get(0)?.otp ?: "")
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@SignUpActivity)
            }
            mIdlingResource?.setIdleState(true)
        })

        viewModel.statesLiveData.observe(this@SignUpActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initSpinners(it.data ?: ArrayList())
                mIdlingResource?.setIdleState(true)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@SignUpActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.addAccountLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                viewModel.saveInstitutionDetail(it.data)
                mIdlingResource?.setIdleState(true)
                navigateToHomeScreen()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@SignUpActivity)
                viewModel.clearUserLoginData()
                mIdlingResource?.setIdleState(true)
            } else {
                viewModel.clearUserLoginData()
                mIdlingResource?.setIdleState(true)
            }
        })
    }

    /**
     * Navigate to [com.app.tyst.ui.authentication.otp.otpsignup.OTPSignUpActivity] for verify phone number
     * @param otp String otp which is send to user's phone number
     */
    private fun navigateToPhoneVerificationScreen(otp: String) {
        startActivity(OTPSignUpActivity.getStartIntent(context = this@SignUpActivity,
                phoneNumber = binding.inputPhoneNumber.getTrimText(),
                otp = otp, request = signUpRequest))
    }

    /**
     * Set Social information, if user signup with social
     */
    private fun setSocialInformation() {

        if (social != null) {
            captureUri = Uri.parse(social?.profileImageUrl.toString())
            if (social?.firstName?.isNotEmpty() == true) {
                binding.inputFirstName.setText(social?.firstName)
            }

            if (social?.lastName?.isNotEmpty() == true) {
                binding.inputLastName.setText(social?.lastName)
            }
            if (social?.emailId?.isNotEmpty() == true) {
                binding.inputEmail.setText(social?.emailId)
            }
            if (social?.name?.isNotEmpty() == true && social?.name?.contains(" ") == false) {
                binding.inputUserName.setText(social?.name)
            }

            binding.inputConfirmPassword.visibility = View.GONE
            binding.inputPassword.visibility = View.GONE
            binding.inputPassword.visibility = View.GONE
            binding.inputConfirmPassword.visibility = View.GONE

//            if (social?.profileImageUrl?.isNotEmpty() == true) {
//                binding.btnAdd.setImageResource(R.drawable.selector_edit_profile)
//                checkAndRequestPermissionGallery(true)
//            }
        }
    }

    /**
     * Show focus on invalid input field
     * @param failType Int
     */
    private fun focusInvalidInput(failType: Int) {
        when (failType) {
            EMAIL_EMPTY -> {
                getString(R.string.alert_enter_email).showSnackBar(this@SignUpActivity)
            }
            EMAIL_INVALID -> {
                getString(R.string.alert_enter_valid_email).showSnackBar(this@SignUpActivity)
            }
            EMAIL_LENGTH -> {
                String.format(getString(R.string.alert_max_email_length),
                        resources.getInteger(R.integer.email_max_length)).showSnackBar(this@SignUpActivity)
            }
            USER_NAME_EMPTY -> {
                getString(R.string.alert_enter_user_name).showSnackBar(this@SignUpActivity)
            }
            USER_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_user_name_length),
                        resources.getInteger(R.integer.user_name_min_length)).showSnackBar(this@SignUpActivity)
            }

            USER_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_user_name_character).showSnackBar(this@SignUpActivity)
            }

            FIRST_NAME_EMPTY -> {
                getString(R.string.alert_enter_first_name).showSnackBar(this@SignUpActivity)
            }
            FIRST_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_first_name_length),
                        resources.getInteger(R.integer.first_name_min_length)).showSnackBar(this@SignUpActivity)
            }

            FIRST_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_first_name_character).showSnackBar(this@SignUpActivity)
            }

            LAST_NAME_EMPTY -> {
                getString(R.string.alert_enter_last_name).showSnackBar(this@SignUpActivity)
            }
            LAST_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_last_name_length),
                        resources.getInteger(R.integer.first_name_min_length)).showSnackBar(this@SignUpActivity)
            }

            LAST_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_last_name_character).showSnackBar(this@SignUpActivity)
            }

            PHONE_NUMBER_EMPTY -> {
                getString(R.string.alert_enter_mobile_number).showSnackBar(this@SignUpActivity)
            }
            PHONE_NUMBER_INVALID -> {
                getString(R.string.alert_invalid_phone_number).showSnackBar(this@SignUpActivity)
            }
            DOB_EMPTY -> {
                getString(R.string.alert_enter_dob).showSnackBar(this@SignUpActivity)
            }
            ADDRESS_EMPTY -> {
                getString(R.string.alert_select_street_address).showSnackBar(this@SignUpActivity)
            }
            STATE_EMPTY -> {
                getString(R.string.alert_select_state).showSnackBar(this@SignUpActivity)
            }
            CITY_EMPTY -> {
                getString(R.string.alert_enter_city).showSnackBar(this@SignUpActivity)
            }
            ZIP_CODE_EMPTY -> {
                getString(R.string.alert_enter_zip_code).showSnackBar(this@SignUpActivity)
            }
            ZIP_CODE_INVALID -> {
                String.format(getString(R.string.alert_min_zip_code_length),
                        resources.getInteger(R.integer.zip_code_min_length)).showSnackBar(this@SignUpActivity)
            }

            ZIP_CODE_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_zip_code_character).showSnackBar(this@SignUpActivity)
            }

            PASSWORD_EMPTY -> {
                getString(R.string.alert_enter_password).showSnackBar(this@SignUpActivity)
            }
            PASSWORD_INVALID -> {
                getString(R.string.alert_valid_password).showSnackBar(this@SignUpActivity)
            }
            CONFORM_PASSWORD_EMPTY -> {
                getString(R.string.alert_enter_confirm_password).showSnackBar(this@SignUpActivity)
            }
            PASSWORD_NOT_MATCH -> {
                getString(R.string.alert_msg_password_and_confirm_password_not_same).showSnackBar(this@SignUpActivity)
            }
            TNC_NOT_ACCEPTED -> {
                getString(R.string.alert_accept_tnc_and_privacy).showSnackBar(this@SignUpActivity)
            }
        }
    }

    private fun performSignUp() {
        signUpRequest = viewModel.getSignUpRequest(userProfileImage = getProfileImageUrl(),
                userName = binding.inputUserName.getTrimText(),
                firstName = binding.inputFirstName.getTrimText(),
                lastName = binding.inputLastName.getTrimText(),
                email = binding.inputEmail.getTrimText(),
                phone = PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()),
                dob = binding.inputDOB.getTrimText().toServerDateFormatString(),
                address = selectedPlace?.address ?: "",
                latitude = (selectedPlace?.latLng?.latitude ?: 0.0).toString(),
                longitude = (selectedPlace?.latLng?.longitude ?: 0.0).toString(),
                city = binding.inputCity.getTrimText(),
                state = if (binding.inputState.getTrimText().isEmpty()) "" else selectedState?.state
                        ?: "",
                stateId = if (binding.inputState.getTrimText().isEmpty()) "" else selectedState?.stateId
                        ?: "",
                zip = binding.inputZipCode.getTrimText(),
                tnc = binding.cbTNC.isChecked,
                password = binding.inputPassword.getTrimText(),
                socialType = social?.type ?: "",
                socialId = social?.socialId ?: "",
                confirmPassword = binding.inputConfirmPassword.getTrimText()
        )

        if (viewModel.isValid(signUpRequest)) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callSignUp(request = signUpRequest, isEncrypted = (application as MainApplication).isEncryptionApply)
        }
    }

    /**
     * Open date picker and set date of birth
     */
    private fun setDOB() {
        mIdlingResource?.setIdleState(false)
        val calendar = Calendar.getInstance()
        if (binding.inputDOB.getTrimText().isNotEmpty()) {
            calendar.time = binding.inputDOB.getTrimText().toMMDDYYYDate()
        }
        val datePicker = DatePickerDialog(this@SignUpActivity, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mIdlingResource?.setIdleState(true)
            binding.inputDOB.setText(viewModel.getDateFromPicker(year, month, dayOfMonth))
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }


    /**
     * Open auto complete place picker to get address
     */
    private fun openPlacePicker() {
        mIdlingResource?.setIdleState(false)
        binding.root.hideKeyBoard()
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("US")
                .build(this)
        startActivityForResult(intent, IConstants.AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IConstants.REQUEST_PLAID_LINK_CODE) {
            /*when (resultCode) {
                Plaid.RESULT_SUCCESS ->
                    (data?.getSerializableExtra(Plaid.LINK_RESULT) as LinkConnection).let {
                        var accountIds = ""
                        var accountNames = ""
                        var accountNumbers = ""
                        it.linkConnectionMetadata.accounts.forEachIndexed { index, linkAccount ->
                            if(index==0){
                                accountIds = linkAccount.accountId
                                accountNames = linkAccount.accountName?:""
                                accountNumbers = linkAccount.accountNumber?:""
                            }else{
                                accountIds += ", " + linkAccount.accountId
                                accountNames += ", " +linkAccount.accountName?:""
                                accountNumbers += ", " +linkAccount.accountNumber?:""
                            }

                        }

                        var info = "##Public Token: "+it.publicToken+"                   ##Account Ids:"+accountIds+"         ##Account Names: "+accountNames+"                   ##Account Numbers: "+accountNumbers+"          ##Institution Id: "+it.linkConnectionMetadata.institutionId+"         ##Institution Name: "+it.linkConnectionMetadata.institutionName
                        logger.debugEvent("Plaid Success", info)
//                        logger.debugEvent("Plaid Success", getString(
//                                R.string.content_success,
//                                it.publicToken,
//                                accountIds,
//                                accountNames,
//                                accountNumbers,
//                                it.linkConnectionMetadata.institutionId,
//                                it.linkConnectionMetadata.institutionName
//                        ))
                        if (isNetworkConnected()) {
                            showHideProgressDialog(true)
                            viewModel.callAddBankAccount(it)
                        } else {
                            mIdlingResource?.setIdleState(true)
                        }
                    }
                Plaid.RESULT_CANCELLED ->
                    (data?.getSerializableExtra(Plaid.LINK_RESULT) as LinkCancellation).let {
                        //                        viewModel.clearUserLoginData()
                        logger.debugEvent("Plaid Cancelled", getString(
                                R.string.content_cancelled,
                                it.institutionId,
                                it.institutionName,
                                it.linkSessionId,
                                it.status
                        ))
                        it.institutionName ?: "" + " Failed after " +
//                        it.status?.showSnackBar(this@SignUpActivity)
                        navigateToHomeScreen()
                        mIdlingResource?.setIdleState(true)
                    }
                Plaid.RESULT_EXIT ->
                    (data?.getSerializableExtra(Plaid.LINK_RESULT) as PlaidApiError).let {
                        //                        viewModel.clearUserLoginData()
                        logger.debugEvent("Plaid Exit", getString(
                                R.string.content_exit,
                                it.displayMessage,
                                it.errorCode,
                                it.errorMessage,
                                it.linkExitMetadata.institutionId,
                                it.linkExitMetadata.institutionName,
                                it.linkExitMetadata.status
                        ))
//                        it.displayMessage?.showSnackBar(this@SignUpActivity)
                        navigateToHomeScreen()
                        mIdlingResource?.setIdleState(true)
                    }
                Plaid.RESULT_EXCEPTION ->
                    (data?.getSerializableExtra(Plaid.LINK_RESULT) as java.lang.Exception).let {
                        viewModel.clearUserLoginData()
                        logger.debugEvent("Plaid Exception", getString(
                                R.string.content_exception,
                                it.javaClass.toString(),
                                it.message
                        ))
//                        it.message?.showSnackBar(this@SignUpActivity)
                        navigateToHomeScreen()
                        mIdlingResource?.setIdleState(true)
                    }

            }*/
        } else if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                IConstants.AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        setAddress(Autocomplete.getPlaceFromIntent(data))
                    }
                    mIdlingResource?.setIdleState(true)
                }

                IConstants.REQUEST_CODE_CAMERA -> {
                    openCropper(captureUri)
                }

                IConstants.REQUEST_CODE_GALLERY -> {
                    val selectedImageUri = data?.data
                    captureUri = selectedImageUri
                    openCropper(captureUri)
                }
                IConstants.REQUEST_CODE_CROP_RESULT -> {
                    /* val path = data?.extras!!.get(IConstants.BUNDLE_CROP_URI) as String
                     captureUri = Uri.parse(path)
                     binding.ivProfileImage.loadCircleImage(path, R.drawable.chat_s_user)*/
                    if (data?.getBooleanExtra(IConstants.BUNDLE_IS_CROP_CANCEL, true) == false) {
                        val path = data.extras!!.get(IConstants.BUNDLE_CROP_URI) as String
                        captureUri = Uri.parse(path)
                        binding.ivProfileImage.loadCircleImage(path, R.drawable.user_profile)
                        binding.btnAdd.setImageResource(R.drawable.selector_edit_profile)
                    } else {
                        captureUri = null
                    }
                    mIdlingResource?.setIdleState(true)
                }

                else -> {
                    mediaHelper?.onActivityResult(requestCode, resultCode, data!!)
                }
            }


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            if (data != null) {
                val status = Autocomplete.getStatusFromIntent(data)
                LOGApp.i("Place Picker", status.statusMessage ?: "")
            }
            mIdlingResource?.setIdleState(true)

        } else if (resultCode == Activity.RESULT_CANCELED) {
            LOGApp.i("Place Picker", "Result Canceled")
            mIdlingResource?.setIdleState(true)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Set address in input fields after select address from places api
     * @param place Place
     */
    private fun setAddress(place: Place) {
        selectedPlace = place
        val locationAddress = viewModel.getParseAddress(placeAddress = place.address)
        binding.apply {
            inputAddress.setText(place.address)
            inputCity.setText(locationAddress.city)
            inputZipCode.setText(locationAddress.zipCode)
            setSpinners(locationAddress.state)
        }
        LOGApp.e("Place Result", place.address ?: "")
    }

    /**
     * this function check permissions for camera and storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionCamera() {
        mIdlingResource?.setIdleState(false)
        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                LOGApp.d("", "Permission denied by system")
                val builder = AlertDialog.Builder(this@SignUpActivity)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(String.format(getString(R.string.msg_we_need_permission_for_camera), getString(R.string.application_name)))
                builder.setPositiveButton("Ok") { _, _ ->
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, IConstants.CAMERA_REQUEST_CODE)
                }
                builder.setNegativeButton(R.string.cancel) { _, _ -> }

                builder.setCancelable(false)

                val alertDialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
            }
            mIdlingResource?.setIdleState(true)
        }

        //Request all permission
        permissionHelper?.requestAll {
            openCamera()
        }
    }

    /**
     * this function check permissions for storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionGallery(isForFB: Boolean) {
//        if (permissionHelper == null) {
        mIdlingResource?.setIdleState(false)
        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 12)


        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                LOGApp.d("", "Permission denied by system")
                val builder = AlertDialog.Builder(this@SignUpActivity)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(String.format(getString(R.string.msg_we_need_permission_for_gallery), getString(R.string.application_name)))
                builder.setPositiveButton("Ok") { _, _ ->
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, IConstants.CAMERA_REQUEST_CODE)
                }
                builder.setNegativeButton(R.string.cancel) { _, _ -> }

                builder.setCancelable(false)

                val alertDialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED)
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY)
            }
            mIdlingResource?.setIdleState(true)
        }
//        }

        //Request all permission
        permissionHelper?.requestAll {
            if (isForFB) {
                binding.ivProfileImage.loadImage(social?.profileImageUrl,
                        R.drawable.user_profile)
                downloadSocialImage(social?.profileImageUrl!!)
            } else {
                openGallery()
            }
        }
    }

    private fun openCamera() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            captureUri = getCameraUri(this@SignUpActivity)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
            startActivityForResult(takePictureIntent, IConstants.REQUEST_CODE_CAMERA)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IConstants.REQUEST_CODE_GALLERY)
    }

    private fun openCropper(sourceUri: Uri?) {
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra(IConstants.BUNDLE_IMG_URI, sourceUri?.toString())
        intent.putExtra(IConstants.BUNDLE_CROP_SQUARE, true)
        intent.putExtra(IConstants.BUNDLE_CROP_MIN_SIZE, arrayOf(500, 500))
        startActivityForResult(intent, IConstants.REQUEST_CODE_CROP_RESULT)
    }

    private fun getProfileImageUrl(): String {
        return ""
//        return if (captureUri == null) {
//            ""
//        } else {
//            captureUri?.toString() ?: ""
//        }
    }

    /**
     * Download user's social media profile picture to local for sign up
     * @param url String
     */
    private fun downloadSocialImage(url: String) {
        Glide.with(this@SignUpActivity)
                .asBitmap()
                .load(url)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        captureUri = Uri.parse(saveBitmapImage(image = resource, imageFileName = "JPEG_" + social?.name + ".jpg", context = applicationContext))
                        LOGApp.e("Social Image saved at " + social?.profileImageUrl)
                        mIdlingResource?.setIdleState(false)
                    }
                })
    }

    /**
     * Set spannable text for TNC and Privacy Policy
     * @param textView TextView
     * @param portions Array<out String>
     */
    private fun setBoldAndColorSpannable(textView: TextView, vararg portions: String) {
        val label = textView.text.toString()
        val spannableString1 = SpannableString(label)
        for (portion in portions) {
            val startIndex = label.indexOf(portion)
            val endIndex = startIndex + portion.length
            try {
                if (portion.equals(getString(R.string.terms_n_conditions), true))
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternetConnected(this@SignUpActivity)) {

                                startActivity(StaticPagesActivity.getStartIntent(this@SignUpActivity, IConstants.STATIC_PAGE_TERMS_CONDITION, getString(R.string.terms_n_conditions)))
                            } else {
                                getString(R.string.msg_check_internet_connection).showSnackBar(this@SignUpActivity, IConstants.SNAKBAR_TYPE_ERROR)

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = true // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                else if (portion.equals(getString(R.string.privacy_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternetConnected(this@SignUpActivity)) {

                                startActivity(StaticPagesActivity.getStartIntent(this@SignUpActivity, IConstants.STATIC_PAGE_PRIVACY_POLICY, getString(R.string.privacy_policy)))
                            } else {
                                getString(R.string.msg_check_internet_connection).showSnackBar(this@SignUpActivity, IConstants.SNAKBAR_TYPE_ERROR)

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = true // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                spannableString1.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@SignUpActivity, R.color.white)), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.highlightColor = Color.TRANSPARENT
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        textView.text = spannableString1
    }


    override fun onBackPressed() {
        performBack()
    }

    /**
     * Ask confirmation message to discard any changes and go back to previous screen
     */
    private fun performBack() {
        DialogUtil.alert(context = this@SignUpActivity, title = "", msg = getString(R.string.msg_discard_registration),
                positiveBtnText = getString(R.string.yes), negativeBtnText = getString(R.string.no),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        finish()
                    }

                    override fun onCancel(isNeutral: Boolean) {

                    }
                }, isCancelable = false)
    }

    /*private fun openPlaidSdk() {
        Plaid.setLinkEventListener(linkEventListener = LinkEventListener {
            LOGApp.e("Event", it.toString())
        })
        Plaid.openLink(
                this,
                viewModel.getLinkConfiguration(),
                IConstants.REQUEST_PLAID_LINK_CODE
        )
    }*/

    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }
        return mIdlingResource as RemoteIdlingResource
    }
}
