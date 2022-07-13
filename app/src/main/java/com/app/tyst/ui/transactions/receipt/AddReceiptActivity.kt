package com.app.tyst.ui.transactions.receipt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.request.AddReceiptRequest
import com.app.tyst.data.model.response.CategoryResponse
import com.app.tyst.data.model.response.StatesResponse
import com.app.tyst.databinding.ActivityAddReceiptBinding
import com.app.tyst.databinding.ListItemFeedbackImageBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.imagecropper.CropperActivity
import com.app.tyst.ui.settings.feedback.FeedbackImageModel
import com.app.tyst.utility.IConstants
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
import com.simpleadapter.SimpleAdapter
import com.github.dhaval2404.imagepicker.ImagePicker
import java.util.*
import kotlin.collections.ArrayList

/**
 * This activity is used for add/update receipt
 */
class AddReceiptActivity : BaseActivity() {

    companion object {
        /**
         * Start intent to open AccountTransactionsListActivity
         * @param mContext Context
         * @param isAddReceipt Boolean : Yes -> Add Receipt No-> Update Receipt
         * @return Intent
         */
        fun getStartIntent(mContext: Context, isAddReceipt: Boolean): Intent {
            return Intent(mContext, AddReceiptActivity::class.java).apply {
                putExtra("isAddReceipt", isAddReceipt)
            }
        }
    }

    private val viewModel: AddReceiptViewModel by lazy {
        ViewModelProviders.of(this@AddReceiptActivity).get(AddReceiptViewModel::class.java)
    }

    lateinit var binding: ActivityAddReceiptBinding
    private var permissionHelper: PermissionHelper? = null
    private var captureUri: Uri? = null
    private var mediaHelper: MediaHelper? = null
    private var stateSelected = false // Stop to set State Spinner value of spinner initialization
    private var spinnerClick = false
    private lateinit var adapter: SimpleAdapter<FeedbackImageModel>
    private var categorySelected = false

    private var selectedState: StatesResponse? = null
    private var selectedPlace: Place? = null
    private lateinit var addReceiptRequest: AddReceiptRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@AddReceiptActivity, R.layout.activity_add_receipt)
        initView()
    }

    private fun initView() {
        setFireBaseAnalyticsData("id-addreceiptscreen", "view_addreceiptscreen", "view_addreceiptscreen")
        setIntentData()
        initListener()
        initRecycleView()
        addObserver()
        if ((application as MainApplication).stateList.isEmpty()) {
            showHideProgressDialog(true)
            viewModel.callGetStateList()
        } else {
            initSpinners((application as MainApplication).stateList)
        }

        viewModel.callCategoryList()
    }

    /**
     * Set institutionId, start date and end date from selected account
     */
    private fun setIntentData() {
        binding.tvTitle.text = if (intent?.getBooleanExtra("isAddReceipt", true) != false)
            getString(R.string.add_receipt)
        else
            getString(R.string.update_receipt)
    }

    private fun initSpinners(states: ArrayList<StatesResponse>) {
        viewModel.getStatesList().clear()
        viewModel.getStatesList().addAll(states)
        binding.apply {
            spState.adapter = ArrayAdapter<StatesResponse>(this@AddReceiptActivity, R.layout.item_spinner,
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
            spCategory.adapter = ArrayAdapter<CategoryResponse>(this@AddReceiptActivity, R.layout.item_spinner,
                    android.R.id.text1,
                    category)
            spCategory.onItemSelectedListener = categoryItemListener
        }
    }

    private fun initRecycleView() {
        binding.rvImages.layoutManager = LinearLayoutManager(this@AddReceiptActivity, RecyclerView.HORIZONTAL, false)
        adapter = SimpleAdapter.with<FeedbackImageModel, ListItemFeedbackImageBinding>(R.layout.list_item_feedback_image) { _, model, binding ->
            binding.model = model
        }
        binding.rvImages.adapter = adapter

        adapter.addAll(viewModel.imageList)

        adapter.setClickableViews({ view, _, position ->
            binding.root.hideKeyBoard()
            if (view.id == R.id.btnAddImage) {
                clickAddImage()
            } else if (view.id == R.id.btnRemoveImage) {
                removeSelectedImage(position)
            }
        }, R.id.btnAddImage, R.id.btnRemoveImage)
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

    private fun initListener() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }

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

        }
    }

    private fun clickAddImage() {
        DialogUtil.confirmDialog(context = this@AddReceiptActivity, title = "", msg = getString(R.string.msg_capture_image),
                positiveBtnText = getString(R.string.camera), negativeBtnText = getString(R.string.gallery), il = object : DialogUtil.IL {
            override fun onSuccess() {
                checkAndRequestPermissionCamera()
            }

            override fun onCancel(isNeutral: Boolean) {
                checkAndRequestPermissionGallery()
            }
        })
    }

    private fun addObserver() {
        viewModel.statesLiveData.observe(this@AddReceiptActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initSpinners(it.data ?: ArrayList())
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@AddReceiptActivity)
            }
        })

        viewModel.categoryData.observe(this@AddReceiptActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initCategorySpinners(it.data ?: ArrayList())
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@AddReceiptActivity)
            }
        })

        viewModel.adReceiptLiveData.observe(this@AddReceiptActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                it.settings?.message?.showSnackBar(this@AddReceiptActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                (application as MainApplication).newReceiptAdded.value = true
                Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
                showInterstitial()
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@AddReceiptActivity)
            }
        })

        viewModel.validationObserver.observe(this@AddReceiptActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            if (it.failType != null) {
                focusInvalidInput(it.failType!!)
            }
        })

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

//                IConstants.REQUEST_CODE_CAMERA -> {
//                    openCropper(captureUri)
//                }
//
//                IConstants.REQUEST_CODE_GALLERY -> {
//                    val selectedImageUri = data?.data
//                    captureUri = selectedImageUri
//                    openCropper(captureUri)
//                }
//                IConstants.REQUEST_CODE_CROP_RESULT -> {
//                    val path = data?.extras!!.get(IConstants.BUNDLE_CROP_URI) as String
//                    captureUri = Uri.parse(path)
//                    binding.ivReceiptImage.loadImage(path, R.drawable.user_profile)
//                }

                ImagePicker.REQUEST_CODE -> {
                    val mPaths = data?.getStringArrayListExtra(ImagePicker.getFilePath(data))
                    if (mPaths?.isNotEmpty() == true) {
                        captureUri = Uri.parse(mPaths[0])
                        showSelectedImage(FeedbackImageModel(contentUri = captureUri, imagePath = mPaths[0]))
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
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("US")
                .build(this@AddReceiptActivity)
        startActivityForResult(intent, IConstants.AUTOCOMPLETE_REQUEST_CODE)
    }

    /**not
     * Open date picker and set date of transaction
     */
    private fun setTransactionDate() {
        val calendar = Calendar.getInstance()
        if (binding.inputTransactionDate.getTrimText().isNotEmpty()) {
            calendar.time = binding.inputTransactionDate.getTrimText().toMMDDYYYDate()
        }
        val datePicker = DatePickerDialog(this@AddReceiptActivity, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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
                val builder = AlertDialog.Builder(this@AddReceiptActivity)
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
                val builder = AlertDialog.Builder(this@AddReceiptActivity)
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
        }

        //Request all permission
        permissionHelper?.requestAll {
            openGallery()
        }
    }

    private fun openCamera() {

        ImagePicker.with(this)
            .cameraOnly()	//User can only capture image using Camera
            .compress(1024)//esto hace que la imagen final no pese mas que 1MB
            .maxResultSize(1024, 1024)
            .start()
        overridePendingTransition(0, 0)
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (takePictureIntent.resolveActivity(packageManager) != null) {
//            captureUri = getCameraUri(this@AddReceiptActivity)
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
//            startActivityForResult(takePictureIntent, IConstants.REQUEST_CODE_CAMERA)
//        }
    }

    private fun openGallery() {
        ImagePicker.with(this)
            .galleryOnly()	//User can only select image from Gallery
            .galleryMimeTypes(
                mimeTypes = arrayOf(
                    "image/png"
                )
            )
            .maxResultSize(1024, 1024)
            .start()
        overridePendingTransition(0, 0)

//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, IConstants.REQUEST_CODE_GALLERY)
    }

    private fun getImageUrl(): String {
        return if (captureUri == null) {
            ""
        } else {
            captureUri?.toString() ?: ""
        }
    }

    private fun performAddReceipt() {
        setFireBaseAnalyticsData("id-addreceipt", "click_addreceipt", "click_addreceipt")
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
                getString(R.string.alert_enter_store_name).showSnackBar(this@AddReceiptActivity)
            }
            ADDRESS_EMPTY -> {
                getString(R.string.alert_select_street_address).showSnackBar(this@AddReceiptActivity)
            }

            CITY_EMPTY -> {
                getString(R.string.alert_enter_city).showSnackBar(this@AddReceiptActivity)
            }

            STATE_EMPTY -> {
                getString(R.string.alert_select_state).showSnackBar(this@AddReceiptActivity)
            }

            ZIP_CODE_EMPTY -> {
                getString(R.string.alert_enter_zip_code).showSnackBar(this@AddReceiptActivity)
            }
            ZIP_CODE_INVALID -> {
                String.format(getString(R.string.alert_min_zip_code_length),
                        resources.getInteger(R.integer.zip_code_min_length)).showSnackBar(this@AddReceiptActivity)
            }

            ZIP_CODE_CHARACTER_INVALID -> {
                getString(R.string.alert_invalid_zip_code_character).showSnackBar(this@AddReceiptActivity)
            }


            EMPTY_CATEGORY -> {
                getString(R.string.alert_select_category).showSnackBar(this@AddReceiptActivity)
            }

            DOB_EMPTY -> {
                getString(R.string.alert_select_transaction_date).showSnackBar(this@AddReceiptActivity)
            }

            EMPTY_TOTAL_AMOUNT -> {
                getString(R.string.alert_enter_total_amount).showSnackBar(this@AddReceiptActivity)
            }

//            EMPTY_TAX_AMOUNT -> {
//                getString(R.string.alert_enter_tax_applied).showSnackBar(this@AddReceiptActivity)
//            }

            INVALID_TAX_AMOUNT -> {
//                getString(R.string.alert_invalid_tax_amount).showSnackBar(this@AddReceiptActivity)
                (String.format(getString(R.string.alert_tax_percentage), sharedPreference.maxTaxPercentage) + "%.").showSnackBar(this@AddReceiptActivity)
            }

//            INVALID_TAX_AMOUNT_LIMIT -> {
//                String.format(getString(R.string.alert_tax_percentage), sharedPreference.maxTaxPercentage) + "%.".showSnackBar(this@AddReceiptActivity)
//            }
        }
    }

    /**
     * Show user's selected image in image list
     * @param image Selected image
     */
    private fun showSelectedImage(image: FeedbackImageModel) {
        adapter.clear()
        adapter.addAll(viewModel.getSelectedImage(image))
        adapter.notifyDataSetChanged()
    }

    /**
     * Remove selected images from image list
     * @param position position of selected image
     */
    private fun removeSelectedImage(position: Int) {
        adapter.clear()
        adapter.addAll(viewModel.getImagesAfterRemove(position))
        adapter.notifyDataSetChanged()
    }
}
