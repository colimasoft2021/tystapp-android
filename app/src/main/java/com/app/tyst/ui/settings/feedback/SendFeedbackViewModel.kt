package com.app.tyst.ui.settings.feedback

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.BuildConfig
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.getDeviceName
import com.app.tyst.utility.getDeviceOSVersion
import com.app.tyst.utility.validation.*
import com.dc.retroapi.utils.WebServiceUtils
import com.dc.retroapi.utils.WebServiceUtils.getStringMultipartBodyPart
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * View model call for performing all business logic of [com.app.tyst.ui.feedback.SendFeedbackActivity]
 */

class SendFeedbackViewModel(app: Application) : BaseViewModel(app) {
    var feedbackLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var imageList = ArrayList<FeedbackImageModel>()
    private val MAX_IMAGE = 5

    init {
        imageList.add(FeedbackImageModel())
    }

    /**
     * Validate login inputs
     */
    fun isValid(feedback: String): Boolean {
        return when {
            feedback.isEmpty() -> {
                validationObserver.value = createValidationResult(FEEDBACK_EMPTY, R.id.inputBrief)
                false
            }
            else -> true
        }
    }

    fun callReportProblem(feedback: String) {
        val map = HashMap<String, RequestBody>()
        map["feedback"] = WebServiceUtils.getStringRequestBody(feedback)
        map["images_count"] = WebServiceUtils.getStringRequestBody(imageList.size.toString())
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["app_version"] = WebServiceUtils.getStringRequestBody("Version: " + BuildConfig.VERSION_NAME +"  Version Code: "+BuildConfig.VERSION_CODE)

        SendFeedbackRepository(this).callSendFeedback(map = map,
                files = getImageFiles(),
                feedbackLiveData = feedbackLiveData)
    }

    /**
     * Get MultiPartBody list of selected images
     */
    private fun getImageFiles(): ArrayList<MultipartBody.Part>? {
        val files = ArrayList<MultipartBody.Part>()
        imageList.filter { it.contentUri != null }.forEachIndexed { index, FeedbackImageModel ->
            files.add(getStringMultipartBodyPart("image_" + (index + 1), FeedbackImageModel.contentUri?.toString()
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
}