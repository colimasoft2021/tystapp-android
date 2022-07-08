package com.app.tyst.ui.imagecropper

import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.tyst.R
import com.app.tyst.databinding.ActivityCropperBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.bitmapToFile
import com.app.tyst.utility.extension.getAppMediaFolderPath
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.IOException


/**
 * Created by hb on 26/3/18.
 */

class CropperActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityCropperBinding? = null
    private var imageUri: String? = null
    private var issquare: Boolean = false
    private var minSize: Array<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cropper)
        if (intent.hasExtra(IConstants.BUNDLE_IMG_URI)) {
            imageUri = intent.getStringExtra(IConstants.BUNDLE_IMG_URI)
        } else {
            throw IllegalStateException("you must pass image to crop")
        }
        if (intent.hasExtra(IConstants.BUNDLE_CROP_MIN_SIZE)) {
            minSize = intent.getSerializableExtra(IConstants.BUNDLE_CROP_MIN_SIZE) as Array<Int>
        }
        issquare = intent.getBooleanExtra(IConstants.BUNDLE_CROP_SQUARE, false)


        initListener()

        var rotationDegrees = 0
        try {
            val exifInterface = ExifInterface(imageUri)

            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


        if (issquare) {
            binding?.cropImageView?.setAspectRatio(1, 1)
        } else {
            binding?.cropImageView?.setAspectRatio(10, 4)
        }
        if (minSize != null && minSize?.size == 2) {
            binding?.cropImageView?.setMinCropResultSize(minSize!![0], minSize!![1])
        }
        binding?.cropImageView?.setImageUriAsync(Uri.parse(imageUri))


        // binding!!.cropImageView.setImageBitmap(BitmapFactory.decodeFile(imageUri))

        if (rotationDegrees != 0) {
            binding?.cropImageView?.rotateImage(rotationDegrees)
        }

    }

    private fun initListener() {
        binding?.tvCancel?.setOnClickListener(this)
        binding?.tvDone?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.tvCancel) {
            val destinationPath = getAppMediaFolderPath() + "/" + System.currentTimeMillis() + ".jpg"
            imageUri = bitmapToFile(binding?.cropImageView?.getCroppedImage(0, 0, CropImageView.RequestSizeOptions.NONE), destinationPath).toString()
            val intent = Intent()
            intent.putExtra(IConstants.BUNDLE_CROP_URI, imageUri)
            intent.putExtra(IConstants.BUNDLE_IS_CROP_CANCEL, true)
            setResult(Activity.RESULT_OK, intent)
            finish()

        } else if (i == R.id.tvDone) {
            if (binding!!.cropImageView.croppedImage != null) {
                val destinationPath = getAppMediaFolderPath() + "/" + System.currentTimeMillis() + ".jpg"
                imageUri = bitmapToFile(binding?.cropImageView?.croppedImage, destinationPath).toString()
                val intent = Intent()
                intent.putExtra(IConstants.BUNDLE_CROP_URI, imageUri)
                intent.putExtra(IConstants.BUNDLE_IS_CROP_CANCEL, false)
                setResult(Activity.RESULT_OK, intent)
                finish()

            } else {
                val destinationPath = getAppMediaFolderPath() + "/" + System.currentTimeMillis() + ".jpg"
                imageUri = bitmapToFile(binding?.cropImageView?.getCroppedImage(0, 0, CropImageView.RequestSizeOptions.NONE), destinationPath).toString()
                val intent = Intent()
                intent.putExtra(IConstants.BUNDLE_CROP_URI, imageUri)
                intent.putExtra(IConstants.BUNDLE_IS_CROP_CANCEL, true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }
    }
}
