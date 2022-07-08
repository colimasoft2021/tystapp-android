package com.dc.retroapi.interceptor

import android.text.TextUtils
import android.util.Log
import com.dc.retroapi.Authenticator
import com.dc.retroapi.annotations.RequestExclusionStrategy
import com.dc.retroapi.utils.AESEncrypter
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Invocation
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


/**
 * Created by HB on 21/8/19.

Interceptor used for following things
- Encryption of request parameter - supports GET,POST,DELETE,PUT,PATCH http methods
- Generate checksum based on request parameter and attach to header/body
- Authentication - To send authorization in header and if request is unauthorized then it will retry with refresh token with {n} times

 */


/**  Things to do
 *  Json Support check
 *  Path Support*/

class RequestInterceptor(encryptionKey: String,
                         private val enableEncryptionForAll: Boolean = false,
                         private val enableChecksumForAll: Boolean = false,
                         private val checksumKey: String = "",
                         private val authenticator: Authenticator?) : Interceptor {

    private var aesEncrypt: AESEncrypter = AESEncrypter(encryptionKey)

    override fun intercept(chain: Interceptor.Chain): Response {

        var checksumEnable = this@RequestInterceptor.enableChecksumForAll
        var encryptionEnable = this@RequestInterceptor.enableEncryptionForAll

        var request = chain.request()
        val method = request.method


        /** if particular api doesn't want encryption and/or checksum it can achieve by
        see [RequestExclusionStrategy] */

        request.tag(Invocation::class.java)?.let {
            it.method().getAnnotation(RequestExclusionStrategy::class.java)?.let { tag ->
                val tagValues = TextUtils.split(tag.value, ",").asList()
                checksumEnable = enableChecksumForAll && !tagValues.contains("checksum")
                encryptionEnable = enableEncryptionForAll && !tagValues.contains("encryption")
            }
        }

        if (encryptionEnable || checksumEnable) {
            if (method == "POST") {
                // If Request method is POST
                val requestBody = request.body
                if (requestBody != null) {
                    /*val subType = requestBody.contentType()?.subtype
                    log("Request Body type", subType)*/

                    lateinit var newBody: RequestBody
                    when {
                        request.body is FormBody -> { // if request body is FORM BODY
                            val formBody = request.body as FormBody
                            val size = formBody.size

                            val newBodyBuilder = FormBody.Builder()
                            val sortedTextValueMap = TreeMap<String, String>() // to put all text parameter key/value
                            for (i in 0 until size) {
                                val key = formBody.name(i)
                                val value = formBody.value(i)
                                sortedTextValueMap[key] = value
                                newBodyBuilder.add(key, encryptIfRequired(value, encryptionEnable))
                            }
                            if (checksumEnable) {
                                newBodyBuilder.add(checksumKey, generateChecksum(sortedTextValueMap))
                            }
                            newBody = newBodyBuilder.build()

                        }
                        request.body is MultipartBody -> { //if request body is MULTIPART BODY
                            val formBody = request.body as MultipartBody
                            val partList = formBody.parts

                            val newBodyBuilder = MultipartBody.Builder()
                            newBodyBuilder.setType(MultipartBody.FORM)

                            val sortedTextValueMap = TreeMap<String, String>() // to put all text parameter key/value
                            for (i in partList) {
                                if (i.body.contentType()?.type?.equals("text")!!) { //"text", "image", "audio", "video"
                                    val key = getKeyFromContentDisposition(i.headers?.get("Content-Disposition")!!)
                                    val value = bodyToString(i.body)
                                    sortedTextValueMap[key] = value
                                    newBodyBuilder.addFormDataPart(key, encryptIfRequired(value, encryptionEnable))
                                } else {
                                    val key = getKeyFromContentDisposition(i.headers?.get("Content-Disposition")!!)
                                    sortedTextValueMap[key] = ""
                                    newBodyBuilder.addPart(i)
                                }
                            }

                            if (checksumEnable) {
                                newBodyBuilder.addFormDataPart(checksumKey, generateChecksum(sortedTextValueMap))
                            }
                            newBody = newBodyBuilder.build()
                        }
                        else -> newBody = requestBody
                    }


                    //Check what method type user requested and pass parameters to respective method
                    lateinit var requestBuilder: Request.Builder
                    requestBuilder = when (request.method) {
                        "POST" -> request.newBuilder().post(newBody)
                        "PUT" -> request.newBuilder().put(newBody)
                        "PATCH" -> request.newBuilder().patch(newBody)
                        "DELETE" -> request.newBuilder().delete(newBody)
                        else -> request.newBuilder().post(newBody)
                    }
                    request = requestBuilder.build()
                }
            } else {

                // If Request method is GET
                val fullUrl = request.url
                val sortedTextValueMap = TreeMap<String, String>() // to put all text parameter key/value
                val parameterNames = fullUrl.queryParameterNames

                val parameterIterator = parameterNames.iterator()
                val urlRequestBuilder = request.url.newBuilder()

                while (parameterIterator.hasNext()) {
                    val key = parameterIterator.next()
                    val values = fullUrl.queryParameterValues(key)
                    val value = fullUrl.queryParameterValues(key)[values.lastIndex]//If query has multiple value then dev needs to send in other form
                    sortedTextValueMap[key] = value!!
                    urlRequestBuilder.removeAllQueryParameters(key) // remove already query parameter
                    urlRequestBuilder.addQueryParameter(key, encryptIfRequired(value, encryptionEnable))
                }

                if (checksumEnable) {
                    urlRequestBuilder.addQueryParameter(checksumKey, generateChecksum(sortedTextValueMap))
                }
                request = request.newBuilder().get().url(urlRequestBuilder.build()).build()

            }
            // decrypt encrypted response
            return requestWithRetry(chain, request, encryptionEnable)
        }
        return chain.proceed(request)
    }


    private fun requestWithRetry(chain: Interceptor.Chain, req: Request, encryptionEnable: Boolean): Response {
        var retryTime = 0
        var request = req
        var response: Response
        do {
            val requestBuilder = request.newBuilder()
            val headerMap = authenticator?.provideHeaderMap()

            headerMap?.asIterable()?.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
            request = requestBuilder.build()

            val originalResponse = chain.proceed(request)
            val responseBody = originalResponse.body

            response = if (responseBody != null) {
                val originalResponseString = responseBody.string()
                val decryptedResponseString = decryptIfRequired(originalResponseString, encryptionEnable)
                val newResponseBody = decryptedResponseString.toResponseBody(responseBody.contentType())
                originalResponse.newBuilder().body(newResponseBody).build()
            } else {
                originalResponse
            }

            val isTokenExpired = authenticator?.onNetworkResponse(response.newBuilder().build() /*because user can only touch response body once*/)
                    ?: false
            if (isTokenExpired) {
                authenticator?.onTokenRequest()
                request = request.newBuilder().build() //todo
            }
            retryTime++
        } while (isTokenExpired && retryTime <= authenticator?.retryTime ?: 0)
        return response
    }


    private fun bodyToString(request: RequestBody?): String {
        try {
            val buffer = Buffer()
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    private fun getKeyFromContentDisposition(value: String): String {
        val pat = Pattern.compile("(?<=name=\")\\w+")
        val mat = pat.matcher(value)
        while (mat.find()) {
            return (mat.group())
        }
        return ""
    }


    private fun encryptIfRequired(value: String, encryptionEnable: Boolean): String {
        return if (encryptionEnable) aesEncrypt.encrypt(value) else value
    }

    private fun decryptIfRequired(value: String, encryptionEnable: Boolean): String {
        try {
            return if (encryptionEnable) aesEncrypt.decrypt(value) else value
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    private fun generateChecksum(map: TreeMap<String, String>): String {
        return aesEncrypt.generateChecksumFromSortedMap(map)
    }

    private fun log(tag: String, message: String?) {
        Log.e(tag, message!!)
    }
}
