package com.dc.mvvmskeleton.data.model.hb


import com.google.gson.annotations.SerializedName

import java.util.ArrayList

class WSListResponse<T> : HBBaseResponse() {
    @SerializedName("data")
    var data: ArrayList<T>? = null
}
