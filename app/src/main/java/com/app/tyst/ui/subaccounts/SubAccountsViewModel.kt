package com.app.tyst.ui.subaccounts

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.AccountResponse
import com.app.tyst.ui.core.BaseViewModel

/**
 * View model call for performing all business logic of [com.app.tyst.ui.subaccounts]
 */

class SubAccountsViewModel (app: Application) : BaseViewModel(app) {
    var accountsData = MutableLiveData<WSObserverModel<ArrayList<AccountResponse>>>()

    /**
     * Api call for get all sub accounts of an institution.
     * @param institutionId
     */
    fun callUserAccounts(institutionId: String) {
        val map = HashMap<String, String>()
        map["institution_id"] = institutionId
        SubAccountsRepository(this).callUserAccounts(map, accountsData)
    }
}