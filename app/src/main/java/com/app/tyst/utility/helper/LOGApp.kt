package com.app.tyst.utility.helper

import android.util.Log

object LOGApp {
    var ENABLE_LOG = true //true;
    var LOG_FILE_NAME = "TYST App"
    fun v(strTag: String, strMessage: String) {
        if (ENABLE_LOG) Log.v(LOG_FILE_NAME, strMessage ?: "")
    }

    fun d(strTag: String, strMessage: String) {
        if (ENABLE_LOG) Log.d(strTag, strMessage ?: "")
    }

    fun e(strTag: String, strMessage: String) {
        if (ENABLE_LOG) Log.e(strTag, strMessage ?: "")
    }

    fun e(strMessage: String) {
        if (ENABLE_LOG) Log.e(LOG_FILE_NAME, strMessage ?: "")
    }

    fun w(strTag: String, strMessage: String) {
        if (ENABLE_LOG) Log.w(LOG_FILE_NAME, strMessage)
    }

    fun i(strTag: String, strMessage: String) {
        if (ENABLE_LOG) Log.i(strTag, strMessage ?: "")
    }

    fun println(strMessage: String) {
        if (ENABLE_LOG) println(strMessage ?: "")
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_LOG) Log.e(LOG_FILE_NAME, msg ?: "", tr)
    }

    fun d(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_LOG) Log.e(LOG_FILE_NAME, msg, tr)
    }

    fun wtf(tag: String, msg: String) {
//        if (ENABLE_LOG && !CommonUtils.isEmpty(tag) && !CommonUtils.isEmpty(msg))
//            Log.wtf(LOG_FILE_NAME, msg);
    }

    fun e(msg: String?, e: Throwable?) {
        if (!ENABLE_LOG) return
        Log.e(LOG_FILE_NAME, msg, e)
    }
}