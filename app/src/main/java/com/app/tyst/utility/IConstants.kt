package com.app.tyst.utility

import android.os.Environment
import com.app.tyst.BuildConfig

/**
 * This class contains all the constants value that is used in application
 * All values defined here, it can not be changed in application lifecycle
 */
interface IConstants {
    companion object {

        const val SPLASH_TIME = 1000L//splash time 3second
        const val SNAKE_BAR_SHOW_TIME = 3000L
        val INCLUDE_HEADER = true

        val BASE_URL = BuildConfig.BASE_URL //TODO add your API base url in app/build.gradle

        const val SNAKBAR_TYPE_ERROR = 1
        const val SNAKBAR_TYPE_SUCCESS = 2
        const val SNAKBAR_TYPE_MESSAGE = 3

        const val REQUEST_SEARCH_PLACE = 115
        const val REQUEST_CODE_GALLERY = 500
        const val REQUEST_CODE_CAMERA = 600
        const val REQUEST_CODE_CROP_RESULT = 106
        const val CAMERA_REQUEST_CODE = 103
        const val REQUEST_CODE_CAPTURE_IMAGE = 107

        const val REQUEST_CODE_FACEBOOK_LOGIN = 1010
        const val REQUEST_CODE_GOOGLE_LOGIN = 1011
        const val REQUEST_PLAID_LINK_CODE = 108
        const val REQUEST_DATE_RANGE = 109
        const val AUTOCOMPLETE_REQUEST_CODE = 110
        const val REQUEST_ACCOUNT = 112
        const val REQUEST_CATEGORY = 113

        val MEDIA_TYPE_IMAGE = 1
        val storageDir = Environment.getExternalStorageDirectory().toString() + "/tyst/"
        val IMAGES_FOLDER_PATH = "$storageDir/Images"
        val IMAGE_DIRECTORY_NAME = ".tyst"
        const val FOLDER_NAME = "tyst"

        const val SP_NOTIFICATION_CHANNEL_DEFAULT = "sp_notification_channel_default"
        val STATIC_PAGE_ABOUT_US: String = "aboutus"
        val STATIC_PAGE_TERMS_CONDITION: String = "termsconditions"
        val STATIC_PAGE_PRIVACY_POLICY: String = "privacypolicy"
        val STATIC_PAGE_EULA: String = "eula"

        const val BUNDLE_CROP_URI = "bundle_crop_uri"
        const val BUNDLE_IS_CROP_CANCEL = "is_crop_cancel"
        const val BUNDLE_IMG_URI = "bundle_img_uri"
        const val BUNDLE_CROP_SQUARE = "bundle_crop_square"
        const val BUNDLE_CROP_MIN_SIZE = "bundle_crop_min_size"


        const val SOCIAL_TYPE_FB = "facebook"
        const val SOCIAL_TYPE_GOOGLE = "google"
        const val DEVICE_TYPE_ANDROID = "android"


        const val PARAM_NOTIFICATION_TYPE = "type"

        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_EMAIL_SOCIAL = "email_social"
        const val LOGIN_TYPE_PHONE = "phone"
        const val LOGIN_TYPE_PHONE_SOCIAL = "phone_social"

        const val COUNT_DOWN_TIMER = 1000L // 30 second
        const val BUNDLE_SELECTED_IMAGE = "bundle_selected_image"

        const val ENABLED = "Enabled"
        const val DISABLED = "Disabled"

        const val EVENT_CLICK = "Click Event"

        const val EXTRA_TAX_PERCENTAGE = 6

    }
}
