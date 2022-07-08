package com.app.tyst.ui.export

import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LogListResponse
import com.app.tyst.data.remote.ApiClient
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.extension.getAppMediaFolderPath
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.google.gson.JsonElement
import com.hb.logger.Logger
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

/**
 * Repository class for of [com.app.tyst.ui.export.ExportFragment]
 */
class ExportRepository(private val baseViewModel: BaseViewModel) {
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    /**
     * Api call for get logs list.
     */

    fun callLogList(categoryData: MutableLiveData<WSObserverModel<ArrayList<LogListResponse>>>) {
        ApiClient.apiService.callLogList().enqueue(object : Callback<WSListResponse<LogListResponse>> {


            override fun onResponse(call: Call<WSListResponse<LogListResponse>>,
                                    response: Response<WSListResponse<LogListResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<ArrayList<LogListResponse>>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    categoryData.value = observerModel
                } else {
                    val observerModel = WSObserverModel<ArrayList<LogListResponse>>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    categoryData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSListResponse<LogListResponse>>, t: Throwable) {
                val observerModel = WSObserverModel<ArrayList<LogListResponse>>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                categoryData.value = observerModel
            }
        })
    }

    /**
     * Download cvc file.
     */

    fun callDownloadFile(url: String, downloadedPath: MutableLiveData<String>) {
        ApiClient.apiService.downloadTransactionLogFile(url).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    logger.debugEvent("Download Start", " Transaction log file downloading start")
                    val fileName = URLUtil.guessFileName(url, null, null)
                    downloadedPath.value = writeResponseBodyToDisk(response.body(), fileName)
                    logger.debugEvent("File Downloaded", "Transaction log file download was a success? ${downloadedPath.value}")
                } else {
                    logger.debugEvent("Download Failed", "server contact failed ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    /**
     * Api call for delete bank account.
     */

    fun callGenerateLog(map: HashMap<String, String>,
                        logGeneratedLiveData: MutableLiveData<WSObserverModel<JsonElement>>) {
        ApiClient.apiService.callGenerateLog(map).enqueue(object : Callback<WSGenericResponse<JsonElement>> {
            override fun onResponse(call: Call<WSGenericResponse<JsonElement>>,
                                    response: Response<WSGenericResponse<JsonElement>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings = body?.settings
                    observerModel.data = body?.data
                    logGeneratedLiveData.value = observerModel

                } else {
                    val observerModel = WSObserverModel<JsonElement>()
                    observerModel.settings =
                            baseViewModel.getErrorSettings(errorCode = response.code())
                    logGeneratedLiveData.value = observerModel
                }
            }

            override fun onFailure(call: Call<WSGenericResponse<JsonElement>>, t: Throwable) {
                val observerModel = WSObserverModel<JsonElement>()
                observerModel.settings = baseViewModel.getErrorSettings(e = t)
                logGeneratedLiveData.value = observerModel
            }
        })
    }

    /**
     * Save download file in device storage memory
     * @param body ResponseBody?
     * @return String
     */
    fun writeResponseBodyToDisk(body: ResponseBody?, fileName: String): String {
        try {
            val path = getAppMediaFolderPath() + if (fileName.isNotEmpty()) "/$fileName" else "/statement-" + System.currentTimeMillis() + ".csv"
//            val path = getAppMediaFolderPath() + "/statement-" + System.currentTimeMillis() + ".csv"
            val futureStudioIconFile = File(path)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body?.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body?.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)

                while (true) {
                    val read = inputStream?.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read ?: 0)

                    fileSizeDownloaded += read?.toLong() ?: 0

                    logger.debugEvent("File Download", "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream.flush()

                return path
            } catch (e: IOException) {
                logger.dumpCrashEvent(e)
                return ""
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            logger.dumpCrashEvent(e)
            return ""
        }

    }
}