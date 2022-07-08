package com.dc.mvvmskeleton.data.model.hb

import com.google.gson.annotations.SerializedName

class WSObjectResponse<T> : HBBaseResponse() {
    @SerializedName("data")
    var data: T? = null
}
