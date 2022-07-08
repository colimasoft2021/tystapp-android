package com.app.tyst.ui.transactions.transactiondetail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.TransactionDetailResponse
import com.app.tyst.ui.core.BaseViewModel
import com.google.gson.JsonElement

/**
 * View model call for performing all business logic of [com.app.tyst.ui.transactions.transactiondetail.TransactionDetailActivity]
 */
class TransactionDetailViewModel(app: Application) : BaseViewModel(app) {
    var transactionId: String = ""
    var transactionData = MutableLiveData<WSObserverModel<TransactionDetailResponse>>()
    private var transactionDetail: TransactionDetailResponse? = null
    var changeTaxLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var tipAddedLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var deleteTransactionLiveData = MutableLiveData<WSObserverModel<JsonElement>>()
    var updateCategoryLiveData = MutableLiveData<WSObserverModel<JsonElement>>()

    var newTaxValue: String = ""
    var newTipValue: String = ""

    /**
     * Api call for get transaction detail.
     */

    fun callTransactionDetail() {
        val map = HashMap<String, String>().apply {
            this["transaction_id"] = transactionId
        }
        TransactionDetailRepository(this).callTransactionDetail(map, transactionData)
    }

    /**
     * Api call for change tax.
     */

    fun callChangeTax(newTaxValue: String, reasonId: String) {
        this.newTaxValue = newTaxValue
        val map = HashMap<String, String>().apply {
            this["tax_amount"] = newTaxValue
            this["reason_id"] = reasonId
            this["transaction_id"] = transactionId
        }
        TransactionDetailRepository(this).callChangeTax(map, changeTaxLiveData)
    }

    /**
     * Api call for add tip.
     */

    fun callAddTip(newTipValue: String) {
        this.newTipValue = newTipValue
        val map = HashMap<String, String>().apply {
            this["tip_amount"] = newTipValue
            this["transaction_id"] = transactionId
        }
        TransactionDetailRepository(this).callAddTip(map, tipAddedLiveData)
    }

    /**
     * Api call for delete transaction.
     */

    fun callDeleteTransaction() {
        val map = HashMap<String, String>().apply {
            this["transaction_id"] = transactionDetail?.tTransactionsId ?: ""
        }
        TransactionDetailRepository(this).callDeleteTransaction(map, deleteTransactionLiveData)
    }

    /**
     * Api call for delete transaction.
     */

    fun callUpdateCategory(categoryId:String, categoryName:String) {
        val map = HashMap<String, String>().apply {
            this["transaction_id"] = transactionDetail?.tTransactionsId ?: ""
            this["category_id"] = categoryId
            this["category_name"] = categoryName
        }
        TransactionDetailRepository(this).callUpdateCategory(map, updateCategoryLiveData)
    }

    fun setTransactionData(transactionDetail: TransactionDetailResponse?) {
        this.transactionDetail = transactionDetail
    }

    fun getTransactionData() = this.transactionDetail

    /**
     * Change tax value with new entered value
     */
    fun changeTax() {
        this.transactionDetail?.changedTaxAmount = newTaxValue
    }

    /**
     * Change tip value with new entered value
     */
    fun changeTip() {
        this.transactionDetail?.tipAmount = newTipValue
    }
}