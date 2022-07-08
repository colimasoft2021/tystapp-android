package com.app.tyst.ui.settings.staticpages

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.StaticPageResponse
import com.app.tyst.ui.core.BaseViewModel


class StaticPageViewModel(app: Application) : BaseViewModel(app) {


    var staticPageiveData = MutableLiveData<WSObserverModel<StaticPageResponse>>()

    /**
     * Api call Static Page
     */

    fun callStaticPage(pageCode: String) {
        val map = HashMap<String, String>()
        map["page_code"] = pageCode
        StaticPageRepository(this@StaticPageViewModel).callStaticPage(map, staticPageiveData)
    }

    /**
     * Call api for update TNC or Privacy Policy
     */
    fun callUpdateTNCPrivacyPolicy(pageCode: String) {
        val map = HashMap<String, String>()
        map["page_type"] = pageCode
        StaticPageRepository(this@StaticPageViewModel).callUpdateTNCPrivacyPolicy(map)
    }


}

