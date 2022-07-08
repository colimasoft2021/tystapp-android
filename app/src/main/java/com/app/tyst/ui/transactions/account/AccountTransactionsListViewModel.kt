package com.app.tyst.ui.transactions.account

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSGenericResponse
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.AccountTransactionsDataResponse
import com.app.tyst.data.model.response.InstitutionsTotalAmountItem
import com.app.tyst.ui.core.BaseViewModel
import com.google.gson.JsonElement
import retrofit2.Call

/**
 * View model call for performing all business logic of [com.app.tyst.ui.transactions.AccountTransactionsListActivity]
 */
class AccountTransactionsListViewModel(app: Application) : BaseViewModel(app) {

    var institution: InstitutionsTotalAmountItem? = null
    var startDate = "2017-01-1"
    var endDate = "2019-11-07"
    var searchKeyword = ""
    var accountId = ""
    var pageIndex = 1
    var shouldLoadMore = false

    var transactionLiveData = MutableLiveData<WSObserverModel<AccountTransactionsDataResponse>>()
    private var request: Call<WSGenericResponse<JsonElement>>? = null

    /**
     * Api call for get all transaction data of an account.
     */

    fun callTransactionData() {
        val map = HashMap<String, String>().apply {
            this["start_date"] = startDate
            this["end_date"] = endDate
            this["search_keyword"] = searchKeyword
            this["institution_id"] = institution?.institutionId ?: ""
            this["account_id"] = accountId
            this["rec_limit"] = "50"
            this["page_index"] = pageIndex.toString()
        }

        if (request != null && request?.isExecuted == true) {
            request?.cancel()
        }
        request = AccountTransactionsListRepository(this).callTransactionData(map, transactionLiveData)
    }

    fun resetPagination(){
        pageIndex = 1
        shouldLoadMore = false
    }

}