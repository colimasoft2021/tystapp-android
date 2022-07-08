package com.app.tyst.utility.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Check is network available or not
 * @param context Context?
 * @return Boolean return network conectivity status. true/false
 */
fun checkInternetConnected(context: Context?): Boolean {
    return if (context != null) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        activeNetwork?.isConnectedOrConnecting == true
    } else {
        true
    }
}