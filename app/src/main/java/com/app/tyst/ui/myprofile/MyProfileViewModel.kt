package com.app.tyst.ui.myprofile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.extension.sharedPreference
import com.google.gson.JsonElement
//import com.plaid.linkbase.models.LinkConfiguration
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.PlaidProduct
import com.plaid.linkbase.models.configuration.LinkConfiguration
import com.plaid.linkbase.models.configuration.PlaidProduct
import com.plaid.linkbase.models.connection.LinkConnection
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.myprofile.MyProfileFragment]
 */
class MyProfileViewModel(app: Application) : BaseViewModel(app) {
    var addAccountLiveData = MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>()
    var accountsLiveData = MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>()
    var deleteAccountLiveData = MutableLiveData<WSObserverModel<JsonElement>>()

    /**
     * This will return link configuration for open Plaid sdk
     * @return LinkConfiguration
     */
    fun getLinkConfiguration() = LinkConfiguration(
            clientName = app.getString(R.string.application_name),
            products = listOf(PlaidProduct.TRANSACTIONS),
            language = Locale.ENGLISH.language,
            countryCodes = listOf(Locale.US.country)
    )

    /**
     * Call api for add user bank account in our server
     * @param linkConnection LinkConnection
     */
    fun callAddBankAccount(linkConnection: LinkConnection) {
        val map = HashMap<String, String>()
        map["institution_id"] = linkConnection.linkConnectionMetadata.institutionId?:""
        map["public_token"] = linkConnection.publicToken
        map["institution_name"] = linkConnection.linkConnectionMetadata.institutionName?:""
        AddAccountRepository(this).callAddBankAccount(map, addAccountLiveData)
    }

    /**
     * Call api for add user bank account in our server
     * @param linkConnection LinkConnection
     */
    fun callAddBankAccount(map: HashMap<String, String>) {
        AddAccountRepository(this).callAddBankAccount(map, addAccountLiveData)
    }

    /**
     * Call api for delete user bank account in our server
     * @param institutionId id of account, which need to be unlink
     */
    fun callDeleteBankAccount(institutionId: String) {
        val map = HashMap<String, String>()
        map["institution_id"] = institutionId
        AddAccountRepository(this).callDeleteBankAccount(map, deleteAccountLiveData)
    }

    fun saveInstitutionDetail(plaidInstitutionDetail: ArrayList<PlaidInstitutionResponse>?) {
        sharedPreference.setInstitutionsList(plaidInstitutionDetail)
    }

    /**
     * Save list of added bank account after remove one bank accont
     * @param plaidInstitutionDetail ArrayList<PlaidInstitutionResponse>?
     */
    fun saveBankListAfterRemoveBankAccount(plaidInstitutionDetail: ArrayList<PlaidInstitutionResponse>?) {
        if (plaidInstitutionDetail?.isNullOrEmpty() == false) {
            val accountList = ArrayList<PlaidInstitutionResponse>()
            accountList.addAll(plaidInstitutionDetail)
            accountList.removeAt(0)
            sharedPreference.setInstitutionsList(accountList)
        }
    }

    /**
     * Api call for get user added accounts
     */
    fun callGetBankAccount(){
        AddAccountRepository(this).callGetBankAccount(accountsLiveData)
    }

}