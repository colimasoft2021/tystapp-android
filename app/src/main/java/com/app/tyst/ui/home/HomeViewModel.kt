package com.app.tyst.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.request.GetPlaidTransactionsRequest
import com.app.tyst.data.model.response.*
import com.app.tyst.data.model.response.plaid.PlaidTransactionsResponse
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.toServerDateFormatString
import com.google.gson.JsonElement
import com.plaid.linkbase.models.configuration.LinkConfiguration
import com.plaid.linkbase.models.configuration.PlaidProduct
import com.plaid.linkbase.models.connection.LinkConnection
//import com.plaid.linkbase.models.LinkConfiguration
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.PlaidProduct
import java.util.*
import kotlin.collections.HashMap

/**
 * View model call for performing all business logic of [com.app.tyst.ui.home.HomeActivity] and [com.app.tyst.ui.home.HomeFragment]
 */
class HomeViewModel(app: Application) : BaseViewModel(app) {
    var transactionsData = MutableLiveData<PlaidTransactionsResponse>()
    var institutionsTransactionLiveData = MutableLiveData<WSObserverModel<InstitutionsTransactionResponse>>()
    var fetchTransactionData = MutableLiveData<WSObserverModel<JsonElement>>()
    var plaidSettingDate = MutableLiveData<WSObserverModel<PlaidSettingResponse>>()
    var subscriptionStatusData = MutableLiveData<WSObserverModel<SubscriptionStatusResponse>>()
    var addAccountLiveData = MutableLiveData<WSObserverModel<ArrayList<PlaidInstitutionResponse>>>()
    var publicTokenData = MutableLiveData<WSObserverModel<PublicTokenResponse>>()
    var institutionErrorResloveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var startDate = "2018-01-1"
    var endDate = "2020-04-04"

    init {
        resetDateRange()
    }

    fun resetDateRange(){
        val strDate = Calendar.getInstance()
        strDate.set(Calendar.DAY_OF_MONTH, 1)
        strDate.add(Calendar.MONTH, -(Calendar.getInstance().get(Calendar.MONTH)+12))
        startDate = strDate.time.toServerDateFormatString()
        endDate = Calendar.getInstance().time.toServerDateFormatString()
    }

    /**
     * Api call for application version checking.
     */

    fun callGetPlaidTransactions(request: GetPlaidTransactionsRequest) {
        HomeRepository(this).callGetPlaidTransactions(request, transactionsData)
    }

    fun callAllInstitutionsTransactions() {
        val map = HashMap<String, String>().apply {
            this["start_date"] = startDate
            this["end_date"] = endDate
        }
        HomeRepository(this).callAllInstitutionsTransactions(map, institutionsTransactionLiveData)
    }

    /**
     * Api call for fetch transaction details
     */
    fun callFetchTransactions(institutionId: String) {
        if(institutionId.isNotBlank()) {
            val map = HashMap<String, String>().apply {
                this["institution_id"] = institutionId
            }
            HomeRepository(this).callFetchTransactions(map, fetchTransactionData)
        }
    }

    /**
     * Change transaction dump value from 1 to 0. It indicate that all accounts transaction are stored on our server and we no need to call "fetch_transaction_details" api
     * to store account's transactions on our server.
     */
    fun markedAccountAsRecorded() {
        val transactionList = sharedPreference.getInstitutionsList()
        transactionList?.forEach { trans ->
            trans.isFirstTransactionsDumped = "0"
        }
        sharedPreference.setInstitutionsList(transactionList)
    }

    /**
     * Api call for get plaid settings
     */
    fun callPlaidSettings(institutionId: String) {
       HomeRepository(this).callPlaidSettings(plaidSettingDate)
    }

    /**
     * Api call for get subscription status
     */
    fun callSubscriptionStatus() {
        HomeRepository(this).callSubscriptionStatus(subscriptionStatusData)
    }

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
     * This will return link configuration for open Plaid sdk for re-authenticate user's institution
     * @return LinkConfiguration
     */
    fun getLinkReAuthenticationConfiguration(publicToken:String) = LinkConfiguration(
            clientName = app.getString(R.string.application_name),
            products = listOf(PlaidProduct.TRANSACTIONS),
            language = Locale.ENGLISH.language,
            countryCodes = listOf(Locale.US.country)
            ,publicToken = publicToken
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

    fun saveInstitutionDetail(plaidInstitutionDetail: ArrayList<PlaidInstitutionResponse>?) {
        sharedPreference.setInstitutionsList(plaidInstitutionDetail)
    }

    /**
     * Api Call for Get public token of an institution for re-authenticate it
     */
    fun callGeneratePublicToken(account: InstitutionsTotalAmountItem) {
        val map = HashMap<String, String>()
        map["instituion_id"] = account.uniqueInstitutionId?:""
        HomeRepository(this).callGeneratePublicToken(map, publicTokenData)
    }

    /**
     * Api call for update institution error status
     */

    fun callUpdateInstitutionErrorLog(institutionId:String){
        val map = HashMap<String, String>()
        map["institution_id"] =institutionId
        map["status"] = "Resolved"
        HomeRepository(this).callUpdateInstitutionErrorLog(map, institutionErrorResloveData)
    }

}