package com.dc.mvvmskeleton.data.model.hb

import com.app.tyst.data.model.hb.Settings


class HBPagingUtils {

    var settings: Settings? = null

    companion object {
        fun getInstance(): HBPagingUtils {
            return HBPagingUtils()
        }
    }

    fun getNextPageCount(): String {
        return ((settings?.currPage?.toInt(0) ?: 0) + 1).toString()
    }

    fun isFirstPage(): Boolean {
        return "1" == settings?.currPage ?: "1"
    }

    fun isPageRemaining(): Boolean {
        return "0" != settings?.nextPage ?: "0"
    }
}
