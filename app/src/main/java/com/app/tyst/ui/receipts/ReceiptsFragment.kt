package com.app.tyst.ui.receipts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.request.AddReceiptRequest
import com.app.tyst.data.model.response.CategoryResponse
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.databinding.FragmentReceiptsBinding
import com.app.tyst.ui.core.BaseFragment
import com.app.tyst.ui.imagecropper.CropperActivity
import com.app.tyst.ui.transactions.receipt.AddReceiptViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.mediahelper.MediaHelper
import com.app.tyst.utility.validation.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.master.permissionhelper.PermissionHelper
import com.github.dhaval2404.imagepicker.ImagePicker
import com.app.tyst.utility.helper.LOGApp
import java.util.*
import kotlin.collections.ArrayList

class ReceiptsFragment : BaseFragment<FragmentReceiptsBinding>() {


    private val viewModel: AddReceiptViewModel by lazy {
        ViewModelProviders.of(this@ReceiptsFragment).get(AddReceiptViewModel::class.java)
    }
    lateinit var binding: FragmentReceiptsBinding
    private var permissionHelper: PermissionHelper? = null
    private var captureUri: Uri? = null
    private var mediaHelper: MediaHelper? = null
    private var stateSelected = false // Stop to set State Spinner value of spinner initialization
    private var spinnerClick = false

    private var categorySelected = false

    private var selectedState: StatesResponse? = null
    private var selectedPlace: Place? = null
    private lateinit var addReceiptRequest: AddReceiptRequest

    var imagePicker: ImageView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentFragment(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_receipts
    }

    override fun iniViews() {
        binding = getViewDataBinding()
       /* mBaseActivity?.showAdMob(binding.adView)
        initListener()
        addObserver()
        if ((mBaseActivity?.application as MainApplication).stateList.isEmpty()) {
            showHideProgressDialog(true)
            viewModel.callGetStateList()
        } else {
            initSpinners((mBaseActivity?.application as MainApplication).stateList)
        }
        viewModel.callCategoryList()*/
    }

    override fun initListener() {
        binding.apply {

            btnSave.makeBounceable()
            btnSave.clickWithDebounce { performAddReceipt() }

            inputPlace.clickWithDebounce {
                openPlacePicker()
            }

            inputState.clickWithDebounce {
                if (viewModel.getStatesList().isEmpty()) {
                    spinnerClick = true
                    showHideProgressDialog(true)
                    viewModel.callGetStateList()
                } else {
                    spState.performClick()
                }
            }

            inputCategory.clickWithDebounce {
                if (viewModel.getCategoryList().isEmpty()) {
                    showHideProgressDialog(true)
                    viewModel.callCategoryList()
                } else {
                    spCategory.performClick()
                }
            }

            inputTransactionDate.clickWithDebounce {
                setTransactionDate()
            }

            btnAddImage.clickWithDebounce {
                binding.root.hideKeyBoard()
                DialogUtil.confirmDialog(context = mBaseActivity!!, title = "", msg = getString(R.string.msg_capture_image),
                        positiveBtnText = getString(R.string.camera), negativeBtnText = getString(R.string.gallery), il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        checkAndRequestPermissionCamera()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        checkAndRequestPermissionGallery()
                    }
                })
            }
        }
    }

    fun addObserver() {
        // Listen user has purchased ad free and remove add from this screen
        (mBaseActivity?.application as MainApplication).isAdRemoved.observe(this@ReceiptsFragment, Observer {
            if (it) {
                binding.adView.visibility = View.GONE
            }else{
                binding.adView.visibility = View.VISIBLE
            }
        })

        // Observer for notify interstitial add close
        (mBaseActivity?.application as MainApplication).addClose.observe(this@ReceiptsFragment, Observer {
            mIdlingResource?.setIdleState(false)
        })

        viewModel.statesLiveData.observe(this@ReceiptsFragment, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initSpinners(it.data ?: ArrayList())
            } else if (mBaseActivity?.handleApiError(it.settings) == false) {
                it?.settings?.message?.showSnackBar(mBaseActivity)
            }
        })

        viewModel.categoryData.observe(this@ReceiptsFragment, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initCategorySpinners(it.data ?: ArrayList())
            } else if (mBaseActivity?.handleApiError(it.settings) == false) {
                it?.settings?.message?.showSnackBar(mBaseActivity)
            }
        })

        viewModel.adReceiptLiveData.observe(this@ReceiptsFragment, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                it.settings?.message?.showSnackBar(mBaseActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                getHomeActivity().newReceiptAdded.value = true
            } else if (mBaseActivity?.handleApiError(it.settings) == false) {
                it?.settings?.message?.showSnackBar(mBaseActivity)
            }
        })

        viewModel.validationObserver.observe(this@ReceiptsFragment, Observer {
            binding.root.focusOnField(it.failedViewId)
            if (it.failType != null) {
                focusInvalidInput(it.failType!!)
            }
        })
    }

    private fun initSpinners(states: ArrayList<StatesResponse>) {
        viewModel.getStatesList().clear()
        viewModel.getStatesList().addAll(states)
        binding.apply {
            spState.adapter = ArrayAdapter<StatesResponse>(mBaseActivity!!, R.layout.item_spinner,
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

    private fun initCategorySpinners(category: ArrayList<CategoryResponse>) {
        viewModel.getCategoryList().clear()
        viewModel.getCategoryList().addAll(category)
        binding.apply {
            spCategory.adapter = ArrayAdapter<CategoryResponse>(mBaseActivity!!, R.layout.item_spinner,
                    android.R.id.text1,
                    category)
            spCategory.onItemSelectedListener = categoryItemListener
        }
    }

    /**
     * Item listener for select state
     */
    private val statesItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (stateSelected) {
                binding.inputState.setText(viewModel.getStatesList()[position].state)
                selectedState = viewModel.getStatesList()[position]
            } else {
                stateSelected = true
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    /**
     * Item listener for select category
     */
    private val categoryItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (categorySelected) {
                binding.inputCategory.setText(viewModel.getCategoryList()[position].category)
            } else {
                categorySelected = true
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                IConstants.AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        setAddress(Autocomplete.getPlaceFromIntent(data))
                    }
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
                    val path = data?.extras!!.get(IConstants.BUNDLE_CROP_URI) as String
                    captureUri = Uri.parse(path)
                    binding.ivReceiptImage.loadImage(path, R.drawable.user_profile)
                    binding.ivAddImage.visibility = View.GONE
                }

                ImagePicker.REQUEST_CODE -> {
                    val mPaths = data?.getStringArrayListExtra(ImagePicker.getFilePath(data))
                    if (mPaths?.isNotEmpty() == true) {
                        captureUri = Uri.parse(mPaths[0])
                        binding.ivReceiptImage.loadImage(mPaths[0], R.drawable.user_profile)
                    }
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

        } else if (resultCode == Activity.RESULT_CANCELED) {
            LOGApp.i("Place Picker", "Result Canceled")
        }

    }

    /**
     * Open auto complete place picker to get address
     */
    private fun openPlacePicker() {
        binding.root.hideKeyBoard()
        Places.initialize(mBaseActivity!!.applicationContext, getString(R.string.google_places_api_key))
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("US")
                .build(mBaseActivity!!)
        startActivityForResult(intent, IConstants.AUTOCOMPLETE_REQUEST_CODE)
    }

    /**
     * Open date picker and set date of transaction
     */
    private fun setTransactionDate() {
        val calendar = Calendar.getInstance()
        if (binding.inputTransactionDate.getTrimText().isNotEmpty()) {
            calendar.time = binding.inputTransactionDate.getTrimText().toMMDDYYYDate()
        }
        val datePicker = DatePickerDialog(mBaseActivity!!, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            binding.inputTransactionDate.setText(viewModel.getDateFromPicker(year, month, dayOfMonth))
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.show()
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
            inputPlace.setText(place.address)
            inputCity.setText(locationAddress.city)
            inputZipCode.setText(locationAddress.zipCode)
            setSpinners(locationAddress.state)
        }
        logger.debugEvent("Place Result", place.address ?: "")
    }

    /**
     * this function check permissions for camera and storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionCamera() {
        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                logger.debugEvent("", "Permission denied by system")
                val builder = AlertDialog.Builder(mBaseActivity!!)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(String.format(getString(R.string.msg_we_need_permission_for_camera), getString(R.string.application_name)))
                builder.setPositiveButton("Ok") { _, _ ->
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", mBaseActivity?.packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, IConstants.CAMERA_REQUEST_CODE)
                }
                builder.setNegativeButton(R.string.cancel) { _, _ -> }

                builder.setCancelable(false)

                val alertDialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
            }
        }

        //Request all permission
        permissionHelper?.requestAll {
            openCamera()
        }
    }

    /**
     * This function check permissions for storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionGallery() {
//        if (permissionHelper == null) {
        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 12)


        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                logger.debugEvent("", "Permission denied by system")
                val builder = AlertDialog.Builder(mBaseActivity!!)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(String.format(getString(R.string.msg_we_need_permission_for_gallery), getString(R.string.application_name)))
                builder.setPositiveButton("Ok") { _, _ ->
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", mBaseActivity?.packageName, null)
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
        }

        //Request all permission
        permissionHelper?.requestAll {
            openGallery()
        }
    }

    private fun openCamera() {

//        ImagePicker.Builder(mBaseActivity)
//                .mode(ImagePicker.Mode.CAMERA)
//                .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
//                .directory(ImagePicker.Directory.DEFAULT)
//                .extension(ImagePicker.Extension.PNG)
//                .scale(1024, 1024)
//                .allowMultipleImages(false)
//                .enableDebuggingMode(true)
//                .build()
        mBaseActivity?.let {
            ImagePicker.with(it)
                .cameraOnly()
                .compress(1024)//esto hace que la imagen final no pese mas que 1MB
                .maxResultSize(1024, 1024)
                .start()
        }
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (takePictureIntent.resolveActivity(packageManager) != null) {
//            captureUri = getCameraUri(this@AddReceiptActivity)
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
//            startActivityForResult(takePictureIntent, IConstants.REQUEST_CODE_CAMERA)
//        }
    }

    private fun openGallery() {
//        ImagePicker.Builder(mBaseActivity)
//                .mode(ImagePicker.Mode.GALLERY)
//                .compressLevel(ImagePicker.ComperesLevel.NONE)
//                .directory(ImagePicker.Directory.DEFAULT)
//                .extension(ImagePicker.Extension.PNG)
//                .scale(1024, 1024)
//                .allowMultipleImages(false)
//                .enableDebuggingMode(true)
//                .build()
        mBaseActivity?.let {
            ImagePicker.with(it)
                .galleryOnly()
                .galleryMimeTypes(
                    mimeTypes = arrayOf(
                        "image/png"
                    )
                )
                .maxResultSize(1024, 1024)
                .start()
        }
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, IConstants.REQUEST_CODE_GALLERY)
    }

    /**
     * Open cropper activity for crop an image
     * @param sourceUri Path of an image
     */
    private fun openCropper(sourceUri: Uri?) {
        val intent = Intent(mBaseActivity, CropperActivity::class.java)
        intent.putExtra(IConstants.BUNDLE_IMG_URI, sourceUri?.toString())
        intent.putExtra(IConstants.BUNDLE_CROP_SQUARE, true)
        intent.putExtra(IConstants.BUNDLE_CROP_MIN_SIZE, arrayOf(500, 500))
        startActivityForResult(intent, IConstants.REQUEST_CODE_CROP_RESULT)
    }

    private fun getImageUrl(): String {
        return if (captureUri == null) {
            ""
        } else {
            captureUri?.toString() ?: ""
        }
    }

    private fun performAddReceipt() {
        addReceiptRequest = viewModel.getAddReceiptRequest(receiptImage = getImageUrl(),
                category = binding.inputCategory.getTrimText(),
                storeName = binding.inputStoreName.getTrimText(),
                location = binding.inputPlace.getTrimText(),
                city = binding.inputCity.getTrimText(),
                stateId = if (binding.inputState.getTrimText().isEmpty()) "" else selectedState?.stateId
                        ?: "",
                zipCode = binding.inputZipCode.getTrimText(),
                amount = binding.inputTotalAmount.getNumericValue()?.toString() ?: "",
                transactionDate = binding.inputTransactionDate.getTrimText().toServerDateFormatString(),
                latitude = (selectedPlace?.latLng?.latitude ?: 0.0).toString(),
                longitude = (selectedPlace?.latLng?.longitude ?: 0.0).toString(),
                taxAmount = binding.inputTaxApplied.getNumericValue()?.toString() ?: ""

        )

        if (viewModel.isValid(addReceiptRequest)) {
            showHideProgressDialog(true)
            viewModel.callPayByCash(request = addReceiptRequest)
        }
    }

    /**
     * Show focus on invalid input field
     * @param failType Int
     */
    private fun focusInvalidInput(failType: Int) {
        when (failType) {
            EMPTY_STORE_NAME -> {
                getString(R.string.alert_enter_store_name).showSnackBar(mBaseActivity)
            }
            ADDRESS_EMPTY -> {
                getString(R.string.alert_select_street_address).showSnackBar(mBaseActivity)
            }

            CITY_EMPTY -> {
                getString(R.string.alert_enter_city).showSnackBar(mBaseActivity)
            }

            STATE_EMPTY -> {
                getString(R.string.alert_select_state).showSnackBar(mBaseActivity)
            }

            ZIP_CODE_EMPTY -> {
                getString(R.string.alert_enter_zip_code).showSnackBar(mBaseActivity)
            }
            ZIP_CODE_INVALID -> {
                String.format(getString(R.string.alert_min_zip_code_length),
                        resources.getInteger(R.integer.zip_code_min_length)).showSnackBar(mBaseActivity)
            }

            ZIP_CODE_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_zip_code_character).showSnackBar(mBaseActivity)
            }


            EMPTY_CATEGORY -> {
                getString(R.string.alert_select_category).showSnackBar(mBaseActivity)
            }

            DOB_EMPTY -> {
                getString(R.string.alert_select_transaction_date).showSnackBar(mBaseActivity)
            }

            EMPTY_TOTAL_AMOUNT -> {
                getString(R.string.alert_enter_total_amount).showSnackBar(mBaseActivity)
            }

            EMPTY_TAX_AMOUNT -> {
                getString(R.string.alert_enter_tax_applied).showSnackBar(mBaseActivity)
            }

            INVALID_TAX_AMOUNT -> {
                getString(R.string.alert_invalid_tax_amount).showSnackBar(mBaseActivity)
            }
        }
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