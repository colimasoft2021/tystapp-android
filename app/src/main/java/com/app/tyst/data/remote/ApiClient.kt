package com.app.tyst.data.remote

import com.app.tyst.BuildConfig
import com.app.tyst.MainApplication.Companion.sharedPreference
import com.app.tyst.data.model.hb.Settings
import com.dc.retroapi.builder.RetrofitClientBuilder
import com.dc.retroapi.interceptor.DataConverterInterceptor
import com.dc.retroapi.interceptor.RequestInterceptor
import com.hb.logger.util.LoggerInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by HB on 21/6/19.
 */

class ApiClient {
    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        //private const val PLAID_BASE_URL = BuildConfig.PLAID_BASE_URL
        private const val PLAID_BASE_URL = ""
        val tokenExpireCodes = arrayListOf("-200", "-300", "401", Settings.AUTHENTICATION_ERROR)
        val apiService: ApiService by lazy {
            return@lazy ApiClient().service
        }

        val plaidApiService: ApiService by lazy {
            return@lazy ApiClient().plaidService
        }

        /* val apiService: ApiService by lazy {
             return@lazy RetrofitClientBuilder()
                     .baseUrl(BASE_URL)
                     .connectionTimeout(300)
                     .readTimeout(300)
                     .logLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
 //                    .enableEncryption("CIT@WS!")
 //                    .enableChecksum("ws_checksum")
                     .addConverterFactory(GsonConverterFactory.create())
                     .retroInterceptor(object : RetroInterceptor() {
                         override fun provideHeaderMap(): HashMap<String, String> {
                             val map = HashMap<String, String>()
                             if (sharedPreference.authToken?.isNotEmpty() == true)
                                 map["AUTHTOKEN"] = sharedPreference.authToken.toString()
                             return map
                         }


                     }).create(ApiService::class.java)
         }*/
    }

    private val service: ApiService
        get() {
            return RetrofitClientBuilder()
                    .baseUrl(BASE_URL)
                    .connectionTimeout(300)
                    .readTimeout(300)
                    .writeTimeout(300)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addInterceptor(RequestInterceptor(object : RequestInterceptor.OnRequestInterceptor { //to provide common header & params
                        override fun provideHeaderMap(): HashMap<String, String> {
                            val map = HashMap<String, String>()
                            if (sharedPreference.authToken?.isNotEmpty() == true)
                                map["AUTHTOKEN"] = sharedPreference.authToken.toString()
                            return map
//                            map.put("ws_data", "android")
                        }

                        override fun provideBodyMap(): HashMap<String, String> {
                            return HashMap()
                        }
                    }))
                    .addInterceptor(LoggerInterceptor())
                    .addLogInterceptor(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
                    .addInterceptor(DataConverterInterceptor()) // used for managing data object/array issue @DataAsObject @DataAsList
                    .create(ApiService::class.java)
        }

    private val plaidService: ApiService
        get() {
            return RetrofitClientBuilder()
                    .baseUrl(PLAID_BASE_URL)
                    .connectionTimeout(300)
                    .readTimeout(300)
                    .writeTimeout(300)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addInterceptor(RequestInterceptor(object : RequestInterceptor.OnRequestInterceptor { //to provide common header & params
                        override fun provideHeaderMap(): HashMap<String, String> {
                            return HashMap<String, String>().apply {
                                this["Plaid-Version"] = "2019-05-29"
                                this["User-Agent"] = "Plaid Java 5.1.4"
                                this["Content-Type"] = "application/json"
                            }
                        }

                        override fun provideBodyMap(): HashMap<String, String> {
                            return HashMap()
                        }
                    }))
                    .addInterceptor(LoggerInterceptor())
                    .addLogInterceptor(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
                    .addInterceptor(DataConverterInterceptor()) // used for managing data object/array issue @DataAsObject @DataAsList
                    .create(ApiService::class.java)
        }
}