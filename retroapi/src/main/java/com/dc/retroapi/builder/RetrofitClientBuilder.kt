package com.dc.retroapi.builder

import android.text.TextUtils
import android.util.Log
import com.dc.retroapi.Authenticator
import com.dc.retroapi.interceptor.RequestInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Created by HB on 28/8/19.
 */

class RetrofitClientBuilder {

    private var baseUrl: String = ""
    private var encryptionKey: String = ""
    private var checksumParameterKey: String = ""

    private var logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE
    private var authenticator: Authenticator? = null

    private var connectionTimeOut: Int = 30
    private var readTimeOut: Int = 30
    private var writeTimeOut: Int = 30

    private var converterFactories = arrayListOf<Converter.Factory>()
    private var callAdaptersFactories = arrayListOf<CallAdapter.Factory>()

    fun baseUrl(url: String): RetrofitClientBuilder {
        baseUrl = url
        return this
    }

    fun enableEncryption(encryptionKey: String): RetrofitClientBuilder {
        this.encryptionKey = encryptionKey
        return this
    }

    fun enableChecksum(checksumKey: String): RetrofitClientBuilder {
        checksumParameterKey = checksumKey
        return this
    }

    fun logLevel(logLevel: HttpLoggingInterceptor.Level): RetrofitClientBuilder {
        this@RetrofitClientBuilder.logLevel = logLevel
        return this
    }


    fun authenticator(authenticator: Authenticator): RetrofitClientBuilder {
        this@RetrofitClientBuilder.authenticator = authenticator
        return this
    }


    fun addConverterFactory(factory: Converter.Factory): RetrofitClientBuilder {
        converterFactories.add(factory)
        return this
    }

    fun addCallAdapterFactory(factory: CallAdapter.Factory): RetrofitClientBuilder {
        callAdaptersFactories.add(factory)
        return this
    }

    fun connectionTimeout(timeInSecond: Int): RetrofitClientBuilder {
        this.connectionTimeOut = timeInSecond
        return this
    }

    fun readTimeout(timeInSecond: Int): RetrofitClientBuilder {
        this.readTimeOut = timeInSecond
        return this
    }


    fun writeTimeout(timeInSecond: Int): RetrofitClientBuilder {
        this.writeTimeOut = timeInSecond
        return this
    }

    fun <T> create(service: Class<T>): T {

        val beforeLogging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("PlainRetrofit", message)
            }
        }).apply {
            level = logLevel
        }

        val afterLogging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.e("EncryptedRetrofit", message)
            }
        }).apply {
            level = logLevel
        }

        val parameterInterceptor = RequestInterceptor(encryptionKey,
                !TextUtils.isEmpty(encryptionKey),
                !TextUtils.isEmpty(checksumParameterKey), checksumParameterKey, authenticator)


        val client = OkHttpClient
                .Builder()
                .apply {
                    connectTimeout(connectionTimeOut.toLong(), TimeUnit.SECONDS)
                    readTimeout(readTimeOut.toLong(), TimeUnit.SECONDS)
                    writeTimeout(writeTimeOut.toLong(), TimeUnit.SECONDS)
                    addInterceptor(beforeLogging)
                    addInterceptor(parameterInterceptor)
                    addInterceptor(afterLogging)
                }.build()

        return Retrofit.Builder()
                .apply {
                    client(client)
                    baseUrl(baseUrl)
                    for (factory in converterFactories) {
                        addConverterFactory(factory)
                    }
                }
                .build()
                .create(service)
    }


}
