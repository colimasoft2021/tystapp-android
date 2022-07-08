package com.app.tyst.utility

import android.os.Build

fun getDeviceOSVersion(): String {
    return Build.VERSION.SDK_INT.toString()
}

fun getDeviceOSName(): String {
    val fields = Build.VERSION_CODES::class.java.fields
    return fields[Build.VERSION.SDK_INT].name
}

/**
 * Return device name as HTC One (M8)
 *
 * @return
 */
fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else {
        capitalize(manufacturer) + " " + model
    }
}

private fun capitalize(s: String?): String {
    if (s == null || s.isEmpty()) {
        return ""
    }
    val first = s[0]
    return if (Character.isUpperCase(first)) {
        s
    } else {
        Character.toUpperCase(first) + s.substring(1)
    }
}