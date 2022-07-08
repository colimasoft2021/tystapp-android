package com.app.tyst.ui.settings.editprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.data.model.request.SignUpRequestModel
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.databinding.ActivityEditProfileBinding
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.imagecropper.CropperActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.utility.helper.mediahelper.MediaHelper
import com.app.tyst.utility.validation.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.master.permissionhelper.PermissionHelper
import java.util.*
import kotlin.collections.ArrayList

class EditProfileActivity : BaseActivity() {

    lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel
        get() = ViewModelProviders.of(this).get(EditProfileViewModel::class.java)

    private var permissionHelper: PermissionHelper? = null
    private var captureUri: Uri? = null
    private var mediaHelper: MediaHelper? = null
    private var spinnerClick = false

    private lateinit var signUpRequest: SignUpRequestModel

    private var selectedState: StatesResponse? = null
    private var selectedPlace: Place? = null
    private var stateSelected = false // Stop to set State Spinner value of spinner initialization

    var AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@EditProfileActivity, R.layout.activity_edit_profile)
        initView()
    }

    private fun initView() {
        binding.user = sharedPreference.userDetail
        val config = AppConfig.getSignUpConfiguration()
        if (sharedPreference.appLoginType.equals(IConstants.LOGIN_TYPE_PHONE) || sharedPreference.appLoginType.equals(IConstants.LOGIN_TYPE_PHONE_SOCIAL))
            config.phonenumber.visible = "0"
        binding.config = config
        binding.inputPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher("US"))
        initListeners()
        addObserver()
//        if ((application as MainApplication).stateList.isEmpty()) {
//            showHideProgressDialog(true)
//            mIdlingResource?.setIdleState(false)
//            viewModel.callGetStateList()
//        } else {
//            initSpinners((application as MainApplication).stateList)
//        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce {
                finish()
            }

            btnUpdate.makeBounceable()
            btnUpdate.clickWithDebounce {
                if (isNetworkConnected())
                    performEditProfile()
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
            inputAddress.clickWithDebounce { openPlacePicker() }

            flProfile.clickWithDebounce {
                DialogUtil.confirmDialog(context = this@EditProfileActivity, title = "", msg = getString(R.string.msg_capture_image),
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

    private fun initSpinners(states: ArrayList<StatesResponse>) {
        viewModel.getStatesList().clear()
        viewModel.getStatesList().addAll(states)
        binding.apply {
            spState.adapter = ArrayAdapter<StatesResponse>(this@EditProfileActivity, R.layout.item_spinner,
                    android.R.id.text1,
                    states)
            spState.onItemSelectedListener = statesItemListener
            if (spinnerClick)
                spState.performClick()
            spinnerClick = false
        }

        states.indexOfFirst { state -> state.stateId.equals(sharedPreference.userDetail?.stateId) }.apply {
            if (this != -1) {
                stateSelected = true
                binding.spState.setSelection(this)
            }
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

    private fun addObserver() {

        viewModel.validationObserver.observe(this@EditProfileActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            if (it.failType != null)
                focusInvalidInput(it.failType!!)
        })


        viewModel.statesLiveData.observe(this@EditProfileActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initSpinners(it.data ?: ArrayList())
                mIdlingResource?.setIdleState(true)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@EditProfileActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.editLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it?.data != null) {
                viewModel.saveUserDetails(it.data?.getUserDetails?.get(0))
                viewModel.saveInstitutionDetail(it.data?.getInstitutions)
                it.settings?.message?.showSnackBar(this@EditProfileActivity, type = IConstants.SNAKBAR_TYPE_SUCCESS, duration = IConstants.SNAKE_BAR_SHOW_TIME)
                Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
                mIdlingResource?.setIdleState(true)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@EditProfileActivity)
                mIdlingResource?.setIdleState(true)
            } else {
                mIdlingResource?.setIdleState(true)
            }

        })
    }


    /**
     * Show focus on invalid input field
     * @param failType Int
     */
    private fun focusInvalidInput(failType: Int) {
        when (failType) {

            USER_NAME_EMPTY -> {
                getString(R.string.alert_enter_user_name).showSnackBar(this@EditProfileActivity)
            }
            USER_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_user_name_length),
                        resources.getInteger(R.integer.user_name_min_length)).showSnackBar(this@EditProfileActivity)
            }

            USER_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_user_name_character).showSnackBar(this@EditProfileActivity)
            }

            FIRST_NAME_EMPTY -> {
                getString(R.string.alert_enter_first_name).showSnackBar(this@EditProfileActivity)
            }
            FIRST_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_first_name_length),
                        resources.getInteger(R.integer.first_name_min_length)).showSnackBar(this@EditProfileActivity)
            }

            FIRST_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_first_name_character).showSnackBar(this@EditProfileActivity)
            }

            LAST_NAME_EMPTY -> {
                getString(R.string.alert_enter_last_name).showSnackBar(this@EditProfileActivity)
            }
            LAST_NAME_INVALID -> {
                String.format(getString(R.string.alert_min_last_name_length),
                        resources.getInteger(R.integer.first_name_min_length)).showSnackBar(this@EditProfileActivity)
            }

            LAST_NAME_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_last_name_character).showSnackBar(this@EditProfileActivity)
            }

            DOB_EMPTY -> {
                getString(R.string.alert_enter_dob).showSnackBar(this@EditProfileActivity)
            }
            ADDRESS_EMPTY -> {
                getString(R.string.alert_select_street_address).showSnackBar(this@EditProfileActivity)
            }
            STATE_EMPTY -> {
                getString(R.string.alert_select_state).showSnackBar(this@EditProfileActivity)
            }
            CITY_EMPTY -> {
                getString(R.string.alert_enter_city).showSnackBar(this@EditProfileActivity)
            }
            ZIP_CODE_EMPTY -> {
                getString(R.string.alert_enter_zip_code).showSnackBar(this@EditProfileActivity)
            }
            ZIP_CODE_INVALID -> {
                String.format(getString(R.string.alert_min_zip_code_length),
                        resources.getInteger(R.integer.zip_code_min_length)).showSnackBar(this@EditProfileActivity)
            }

            ZIP_CODE_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_zip_code_character).showSnackBar(this@EditProfileActivity)
            }
        }
    }

    private fun performEditProfile() {
        signUpRequest = viewModel.getEditProfileRequest(userProfileImage = getProfileImageUrl(),
                userName = binding.inputUserName.getTrimText(),
                firstName = binding.inputFirstName.getTrimText(),
                lastName = binding.inputLastName.getTrimText(),
                phoneNumber = if (binding.inputPhoneNumber.getTrimText().isEmpty()) "" else PhoneNumberUtils.normalizeNumber(binding.inputPhoneNumber.getTrimText()),
                dob = binding.inputDOB.getTrimText().toServerDateFormatString(),
                address = if (selectedPlace != null) selectedPlace?.address
                        ?: "" else binding.inputAddress.getTrimText(),
                latitude = if (selectedPlace != null) (selectedPlace?.latLng?.latitude
                        ?: 0.0).toString() else sharedPreference.userDetail?.latitude ?: "0.0",
                longitude = if (selectedPlace != null) (selectedPlace?.latLng?.longitude
                        ?: 0.0).toString() else sharedPreference.userDetail?.longitude ?: "0.0",
                city = binding.inputCity.getTrimText(),
                state = if (binding.inputState.getTrimText().isEmpty()) "" else selectedState?.state
                        ?: "",
                stateId = if (selectedState != null) selectedState?.stateId
                        ?: "" else sharedPreference.userDetail?.stateId ?: "",
                zip = binding.inputZipCode.getTrimText()
        )

        if (viewModel.isValid(signUpRequest)) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callEditProfile(request = signUpRequest)
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
        val datePicker = DatePickerDialog(this@EditProfileActivity, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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
        binding.root.hideKeyBoard()
        mIdlingResource?.setIdleState(false)
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("US")
                .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {

                AUTOCOMPLETE_REQUEST_CODE -> {
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
                    } else {
                        captureUri = null
                    }
                    mIdlingResource?.setIdleState(true)
                }

                else -> {
                    mediaHelper?.onActivityResult(requestCode, resultCode, data!!)
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                if (data != null) {
                    val status = Autocomplete.getStatusFromIntent(data)
                    LOGApp.i("Place Picker", status.statusMessage ?: "")
                }
                mIdlingResource?.setIdleState(true)

            }
            Activity.RESULT_CANCELED -> {
                LOGApp.i("Place Picker", "Result Canceled")
                mIdlingResource?.setIdleState(true)
            }
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
                val builder = AlertDialog.Builder(this@EditProfileActivity)
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
                val builder = AlertDialog.Builder(this@EditProfileActivity)
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
            openGallery()
        }
    }

    private fun openCamera() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            captureUri = getCameraUri(this@EditProfileActivity)
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
