package com.app.tyst.data.model.hb

import com.dc.mvvmskeleton.data.model.hb.HBBaseResponse
import com.google.gson.annotations.SerializedName

class WSGenericResponse<T> : HBBaseResponse() {
    @SerializedName("data")
    var data: T? = null
}
