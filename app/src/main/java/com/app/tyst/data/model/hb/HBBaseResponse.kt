package com.dc.mvvmskeleton.data.model.hb

import com.app.tyst.data.model.hb.Settings
import com.google.gson.annotations.SerializedName

open class HBBaseResponse {
    @SerializedName("settings")
    var settings: Settings? = null
}
