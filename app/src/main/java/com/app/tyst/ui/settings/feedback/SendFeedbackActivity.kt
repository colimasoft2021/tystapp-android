package com.app.tyst.ui.settings.feedback

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.R
import com.app.tyst.databinding.ActivitySendFeedbackBinding
import com.app.tyst.databinding.ListItemFeedbackImageBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.imagecropper.CropperActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.utility.helper.mediahelper.MediaHelper
import com.app.tyst.utility.validation.FEEDBACK_EMPTY
import com.master.permissionhelper.PermissionHelper
import com.simpleadapter.SimpleAdapter
import com.github.dhaval2404.imagepicker.ImagePicker

class SendFeedbackActivity : BaseActivity() {

    lateinit var binding: ActivitySendFeedbackBinding
    private lateinit var adapter: SimpleAdapter<FeedbackImageModel>
    private var permissionHelper: PermissionHelper? = null
    private var captureUri: Uri? = null
    private var mediaHelper: MediaHelper? = null

    private val viewModel: SendFeedbackViewModel
        get() = ViewModelProviders.of(this@SendFeedbackActivity).get(SendFeedbackViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SendFeedbackActivity, R.layout.activity_send_feedback)
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.inputBrief.setOnTouchListener { _, _ ->
            //Called when a touch event is dispatched to a view. This allows listeners to get a chance to respond before the target view.
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.tvFeedback.text = String.format(getString(R.string.msg_report_problem), getString(R.string.application_name))
        initListeners()
        initRecycleView()
        addObserver()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnSend.makeBounceable()
            btnSend.clickWithDebounce { performSend() }
        }
    }

    private fun initRecycleView() {
        binding.rvImages.layoutManager = LinearLayoutManager(this@SendFeedbackActivity, RecyclerView.HORIZONTAL, false)
        adapter = SimpleAdapter.with<FeedbackImageModel, ListItemFeedbackImageBinding>(R.layout.list_item_feedback_image) { _, model, binding ->
            binding.model = model
        }
        binding.rvImages.adapter = adapter

        adapter.addAll(viewModel.imageList)

        adapter.setClickableViews({ view, model, position ->
            if (view.id == R.id.btnAddImage) {
                clickAddImage()
            } else if (view.id == R.id.btnRemoveImage) {
                removeSelectedImage(position)
            }
        }, R.id.btnAddImage, R.id.btnRemoveImage)
    }

    private fun performSend() {
        if (viewModel.isValid(binding.inputBrief.getTrimText())) {
            showHideProgressDialog(true)
            mIdlingResource?.setIdleState(false)
            viewModel.callReportProblem(binding.inputBrief.getTrimText())
        }
    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@SendFeedbackActivity, Observer {
            binding.root.focusOnField(it.failedViewId)
            when (FEEDBACK_EMPTY) {
                it.failType -> {
                    getString(R.string.alert_enter_brief).showSnackBar(this@SendFeedbackActivity)
                }
            }
        })

        viewModel.feedbackLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it.settings?.success == "1") {
                mIdlingResource?.setIdleState(true)
                it.settings?.message?.showSnackBar(this@SendFeedbackActivity, IConstants.SNAKBAR_TYPE_SUCCESS, duration = IConstants.SNAKE_BAR_SHOW_TIME)
                Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
            } else if (!handleApiError(it.settings)) {
                mIdlingResource?.setIdleState(true)
                it?.settings?.message?.showSnackBar(this@SendFeedbackActivity)
            }
        })
    }


    /**
     * Show option for capture image from camera or pick from gallery
     */
    private fun clickAddImage() {
        DialogUtil.confirmDialog(context = this@SendFeedbackActivity, title = "", msg = getString(R.string.msg_capture_image),
                positiveBtnText = getString(R.string.camera), negativeBtnText = getString(R.string.gallery), il = object : DialogUtil.IL {
            override fun onSuccess() {
                checkAndRequestPermissionCamera()
            }

            override fun onCancel(isNeutral: Boolean) {
                checkAndRequestPermissionGallery()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * this function check permissions for camera and storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionCamera() {

        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)

        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                LOGApp.d("", "Permission denied by system")
                val builder = AlertDialog.Builder(this@SendFeedbackActivity)
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
     * this function check permissions for storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionGallery() {
//        if (permissionHelper == null) {

        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 12)


        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                LOGApp.d("", "Permission denied by system")
                val builder = AlertDialog.Builder(this@SendFeedbackActivity)
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
//        }

        //Request all permission
        permissionHelper?.requestAll {
            openGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                /*IConstants.REQUEST_CODE_CAMERA -> {
                    openCropper(captureUri)
                }

                IConstants.REQUEST_CODE_GALLERY -> {
                    val selectedImageUri = data?.data
                    captureUri = selectedImageUri
                    openCropper(captureUri)
                }
                IConstants.REQUEST_CODE_CROP_RESULT -> {
                    if (data?.getBooleanExtra(IConstants.BUNDLE_IS_CROP_CANCEL, true) == false) {
                        val path = (data?.extras?.get(IConstants.BUNDLE_CROP_URI) as String)
                        captureUri = Uri.parse(path)
                        showSelectedImage(FeedbackImageModel(contentUri = captureUri, imagePath = path))
                    } else {
                        captureUri = null
                    }

                }*/

//                ImagePicker.IMAGE_PICKER_REQUEST_CODE -> {
//                    val mPaths = data?.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH)
//                    if (mPaths?.isNotEmpty() == true) {
//                        captureUri = Uri.parse(mPaths[0])
//                        showSelectedImage(FeedbackImageModel(contentUri = captureUri, imagePath = mPaths[0]))
//                    }
//                }
                ImagePicker.REQUEST_CODE -> {
                    val mPaths = data?.getStringArrayListExtra(ImagePicker.getFilePath(data))
                    if (mPaths?.isNotEmpty() == true) {
                        captureUri = Uri.parse(mPaths[0])
                        showSelectedImage(FeedbackImageModel(contentUri = captureUri, imagePath = mPaths[0]))
                    }
                }
                else -> {
                    if (mediaHelper != null) {
                        mediaHelper!!.onActivityResult(requestCode, resultCode, data!!)
                    }
                }
            }
        }
    }

    private fun openCamera() {
//        ImagePicker.Builder(this@SendFeedbackActivity)
//                .mode(ImagePicker.Mode.CAMERA)
//                .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
//                .directory(ImagePicker.Directory.DEFAULT)
//                .extension(ImagePicker.Extension.PNG)
//                .scale(1024, 1024)
//                .allowMultipleImages(false)
//                .enableDebuggingMode(true)
//                .build()
        ImagePicker.with(this)
            .cameraOnly()	//User can only capture image using Camera
            .compress(1024)//esto hace que la imagen final no pese mas que 1MB
            .maxResultSize(1024, 1024)
            .start()
        overridePendingTransition(0, 0)
       /* val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            captureUri = getCameraUri(this@SendFeedbackActivity)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
            startActivityForResult(takePictureIntent, IConstants.REQUEST_CODE_CAMERA)
        }*/
    }

    private fun openGallery() {
//        ImagePicker.Builder(this@SendFeedbackActivity)
//                .mode(ImagePicker.Mode.GALLERY)
//                .compressLevel(ImagePicker.ComperesLevel.NONE)
//                .directory(ImagePicker.Directory.DEFAULT)
//                .extension(ImagePicker.Extension.PNG)
//                .scale(1024, 1024)
//                .allowMultipleImages(false)
//                .enableDebuggingMode(true)
//                .build()
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

    private fun openCropper(sourceUri: Uri?) {
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra(IConstants.BUNDLE_IMG_URI, sourceUri?.toString())
        intent.putExtra(IConstants.BUNDLE_CROP_SQUARE, true)
        intent.putExtra(IConstants.BUNDLE_CROP_MIN_SIZE, arrayOf(500, 500))
        startActivityForResult(intent, IConstants.REQUEST_CODE_CROP_RESULT)
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
