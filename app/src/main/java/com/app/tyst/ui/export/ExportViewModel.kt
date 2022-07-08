package com.app.tyst.ui.export

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.LogListResponse
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.core.BaseViewModel
import com.google.gson.JsonElement

/**
 * View model call for performing all business logic of [com.app.tyst.ui.export.ExportFragment]
 */
class ExportViewModel(app: Application) : BaseViewModel(app) {
    var logsData = MutableLiveData<WSObserverModel<ArrayList<LogListResponse>>>()
    var downloadedPath = MutableLiveData<String>()
    var logGeneratedLiveData = MutableLiveData<WSObserverModel<JsonElement>>()

    /**
     * Api call for get logs list.
     */

    fun callLogList() {
        ExportRepository(this).callLogList(logsData)
    }

    /**
     * Api call for get logs list.
     */

    fun callDownloadFile(fileUrl: String) {
        ExportRepository(this).callDownloadFile(fileUrl, downloadedPath)
    }

    /**
     * Call api for generate transaction logs
     * @param startDate
     * @param endDate
     */
    fun callGenerateLog(startDate: String, endDate: String) {
        val map = HashMap<String, String>()
        map["start_date"] = startDate
        map["end_date"] = endDate
        ExportRepository(this).callGenerateLog(map, logGeneratedLiveData)
    }
}