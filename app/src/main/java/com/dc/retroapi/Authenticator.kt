package com.dc.retroapi

import okhttp3.Response

abstract class Authenticator {
    var retryTime = 0
    /**
     * return true if token is successfully fetched so no retry will occur
     */
    open fun onTokenRequest(): Boolean {
        return true
    }

    /**\
     * return true if token is valid or false
     */
    open fun onNetworkResponse(response: Response): Boolean {
        return false
    }

    /**
     * Return hashmap header values
     */
    open fun provideHeaderMap(): HashMap<String, String> {
        return HashMap()
    }

    fun retryTime(retryTime: Int):Authenticator {
        this.retryTime = retryTime
        return this
    }
}