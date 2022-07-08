package com.app.tyst.ui.transactions.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.CategoryResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.ui.transactions.receipt.AddReceiptRepository

/**
 * View model call for performing all business logic of [com.app.tyst.ui.transactions]
 */
class CategoryViewModel (app: Application) : BaseViewModel(app) {
    var categoryData = MutableLiveData<WSObserverModel<ArrayList<CategoryResponse>>>()

    /**
     * Api call for get category list
     */
    fun callCategoryList() {
        AddReceiptRepository(this).callGetCategoryList(categoryData = categoryData)
    }
}