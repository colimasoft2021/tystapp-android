package com.dc.retroapi.interceptor

import com.dc.retroapi.Authenticator
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by HB on 23/8/19.
 */
class HeaderInterceptor(private val authenticator: Authenticator) : Interceptor {

    /*val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC8xOC4xMzguMTIxLjI0N1wvIiwiYXVkIjoiaHR0cDpcL1wvMTguMTM4LjEyMS4yNDdcLyIsImlhdCI6MTU2NjU3MDg1OSwiZXhwIjoyMDM5OTI1MTM5LCJ1c2VyX2lkIjoiMzQ5IiwidXNlcl9sb2dpbl9pZCI6Njc0fQ.uy1XW3OMgSNz5l2k-u3vpdXhNWVHy-vvEhdVOEJ0l8k"
    "Authorization", "Bearer $token"*/



    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val headerMap = authenticator.provideHeaderMap()
        headerMap.asIterable().forEach {
            requestBuilder.addHeader(it.key, it.value).build()
        }
        return chain.proceed(requestBuilder.build())
    }


    interface HeaderProvider {
        public fun provideHeaderMap(): HashMap<String, String>
    }
}

