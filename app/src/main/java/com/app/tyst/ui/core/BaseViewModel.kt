package com.app.tyst.ui.core

import android.app.Application
import androidx.lifecycle.*
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.Settings
import com.app.tyst.utility.helper.LOGApp
import com.app.tyst.utility.validation.ValidationResult
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * Created by hb on 15/3/18.
 */
open class BaseViewModel(val app: Application) : AndroidViewModel(app), LifecycleObserver {

    fun getErrorSettings(e: Throwable? = null, errorCode: Int = 0): Settings {
        val settings = Settings()
        if (e != null) {
            if (e is HttpException && e.code() == 401) {
                settings.success = Settings.AUTHENTICATION_ERROR
                if ((getApplication<MainApplication>()).getCurrentActivity() is BaseActivity) {
                    (((getApplication<MainApplication>()).getCurrentActivity()) as BaseActivity).showSessionExpireDialog()
                }
            } else if (e is HttpException && e.code() == 500) {
                settings.message = app.getString(R.string.error_500)
                settings.success = Settings.NETWORK_ERROR
            } else if (e is SocketTimeoutException) {
                settings.success = Settings.NETWORK_ERROR
                settings.message = app.getString(R.string.msg_check_internet_connection)
            } else if (e is ConnectException || e is UnknownHostException) {
                settings.message = app.getString(R.string.msg_check_internet_connection)
                settings.success = Settings.NETWORK_ERROR
            } else {
                settings.message = e.toString()
            }
        } else if (errorCode == Settings.AUTHENTICATION_ERROR.toInt()) {
            settings.success = Settings.AUTHENTICATION_ERROR
            if ((getApplication<MainApplication>()).getCurrentActivity() is BaseActivity) {
                (((getApplication<MainApplication>()).getCurrentActivity()) as BaseActivity).showSessionExpireDialog()
            }
        } else if (errorCode == 400) {
            settings.message = app.getString(R.string.error_400)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 403) {
            settings.message = app.getString(R.string.error_403)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 404) {
            settings.message = app.getString(R.string.error_404)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 405) {
            settings.message = app.getString(R.string.error_405)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 406) {
            settings.message = app.getString(R.string.error_406)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 409) {
            settings.message = app.getString(R.string.error_409)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 500) {
            settings.message = app.getString(R.string.error_500)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 503) {
            settings.message = app.getString(R.string.error_503)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 504) {
            settings.message = app.getString(R.string.error_504)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 505) {
            settings.message = app.getString(R.string.error_505)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 300) {
            settings.message = app.getString(R.string.error_300)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 301) {
            settings.message = app.getString(R.string.error_301)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 302) {
            settings.message = app.getString(R.string.error_302)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 303) {
            settings.message = app.getString(R.string.error_303)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 305) {
            settings.message = app.getString(R.string.error_305)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 307) {
            settings.message = app.getString(R.string.error_307)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 410) {
            settings.message = app.getString(R.string.error_410)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 411) {
            settings.message = app.getString(R.string.error_411)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 412) {
            settings.message = app.getString(R.string.error_412)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 413) {
            settings.message = app.getString(R.string.error_413)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 414) {
            settings.message = app.getString(R.string.error_414)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 415) {
            settings.message = app.getString(R.string.error_415)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 416) {
            settings.message = app.getString(R.string.error_416)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 417) {
            settings.message = app.getString(R.string.error_417)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 422) {
            settings.message = app.getString(R.string.error_422)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 501) {
            settings.message = app.getString(R.string.error_501)
            settings.success = Settings.NETWORK_ERROR
        } else if (errorCode == 502) {
            settings.message = app.getString(R.string.error_502)
            settings.success = Settings.NETWORK_ERROR
        } else {
            settings.success = Settings.NETWORK_ERROR
            settings.message = app.getString(R.string.msg_check_internet_connection)
        }

        return settings
    }

    private val TAG = "LifeCycleAwareModel"
    var settingObserver = MutableLiveData<Settings>()

    var validationObserver =  MutableLiveData<ValidationResult>()


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        LOGApp.d(TAG, "onResume() called")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        LOGApp.d(TAG, "onPause() called")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        LOGApp.d(TAG, "onCreate() called")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        LOGApp.d(TAG, "onDestroy() called")
        this.onCleared()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        LOGApp.d(TAG, "onStart() called")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        LOGApp.d(TAG, "onStop() called")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAny() {

    }

}