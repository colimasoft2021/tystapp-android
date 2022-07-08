package com.app.tyst.ui.transactions.changetaxandtip

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.tyst.R
import com.app.tyst.data.model.hb.WSObserverModel
import com.app.tyst.data.model.response.ReasonListResponse
import com.app.tyst.data.model.response.TransactionDetailResponse
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.IConstants.Companion.EXTRA_TAX_PERCENTAGE
import com.app.tyst.utility.validation.*

/**
 * View model call for performing all business logic of [com.app.tyst.ui.transactions.changetaxandtip.ChangeTaxBottomSheetDialog]
 */
class ChangeTaxViewModel(app: Application) : BaseViewModel(app) {
    var reasonData = MutableLiveData<WSObserverModel<ArrayList<ReasonListResponse>>>()
    private var reasons = ArrayList<ReasonListResponse>()
    var changeTax: Boolean = false
    var transaction: TransactionDetailResponse? = null

    fun getReasonList() = reasons


    /**
     * Api call for get category list
     */
    fun callCategoryList() {
        ChangeTaxRepository(this).callGetReasonList(categoryData = reasonData)
    }


    /**
     * Check for valid input field
     * @return Boolean
     */
    fun isValid(amount: String, reason: String): Boolean {
        if (amount.isEmpty()) {
            if (changeTax) {
                validationObserver.value = createValidationResult(EMPTY_TAX_AMOUNT, R.id.inputTaxAmount)
            } else {
                validationObserver.value = createValidationResult(EMPTY_TIP_AMOUNT, R.id.inputTaxAmount)
            }
            return false
        } else if (changeTax && reason.isEmpty()) {
            validationObserver.value = createValidationResult(EMPTY_REASON, R.id.inputReasonText)
            return false
        } else if (!validTaxPercentage(amount)) {
            validationObserver.value = createValidationResult(INVALID_TAX_AMOUNT_LIMIT, R.id.inputTaxAmount)
            return false
        } else if (!validTaxAmount(amount)) {
            validationObserver.value = createValidationResult(INVALID_TAX_AMOUNT, R.id.inputTaxAmount)
            return false
        } else if (!validTipPercentage(amount)) {
            validationObserver.value = createValidationResult(INVALID_TIP_AMOUNT_LIMIT, R.id.inputTaxAmount)
            return false
        } else if (!validTaxPercAfterAddTip(amount)) {
            validationObserver.value = createValidationResult(INVALID_TAX_AMOUNT_LIMIT, R.id.inputTaxAmount)
            return false
        } else if (!validTipAmount(amount)) {
            validationObserver.value = createValidationResult(INVALID_TIP_AMOUNT, R.id.inputTaxAmount)
            return false
        } else if (!validPreTotalAmount(amount)) {
            validationObserver.value = createValidationResult(INVALID_PRE_TOTAL_AMOUNT, R.id.inputTaxAmount)
            return false
        }
        return true
    }

    /**
     * Check for valid tax amount
     * If entered tax amount is greater than PreAmount + Tip amount, then it will give error
     * @return Boolean
     */
    private fun validTaxAmount(amount: String): Boolean {
        if (changeTax &&
                ((amount.toDoubleOrNull()
                        ?: 0.0) > ((transaction?.transactionAmount?.toDoubleOrNull() ?: 0.0) -
                        (transaction?.tipAmount?.toDoubleOrNull() ?: 0.0)))) {
            return false
        }
        return true
    }

    /**
     * Check for valid tax percentage
     * Tax percentage should be less than allowed tax percentage according to states
     * @return Boolean
     */
    private fun validTaxPercentage(amount: String): Boolean {
        if (changeTax && (transaction?.getTaxPercentageDetail(amount)?.toDoubleOrNull() ?: 0.0 > ((transaction?.taxperc?.toDoubleOrNull() ?: 0.0)+EXTRA_TAX_PERCENTAGE)
                        )) {
            return false
        }
        return true
    }

    /**
     * Check for valid tip percentage
     * Tip percentage should be less than allowed maximum tip percentage
     * @return Boolean
     */
    private fun validTipPercentage(amount: String): Boolean {
        if (!changeTax && (transaction?.getTipPercentage(amount)?.toDoubleOrNull() ?: 0.0 > transaction?.tipperc?.toDoubleOrNull() ?: 0.0)) {
            return false
        }
        return true
    }

    /**
     * Check for valid tax percentage after adding tip
     * Calculate tax percentage after adding tip. If tax percentage is more than allowed tax percentage, then this will return false
     * @return Boolean
     */
    private fun validTaxPercAfterAddTip(amount: String): Boolean {

        val taxPerc = transaction?.getTaxPercOnPreTotalDetail(transaction?.getPreTotalAmountDetail(amount)
                ?: 0.0)

        if (!changeTax && (taxPerc?.toDoubleOrNull() ?: 0.0 >  ((transaction?.taxperc?.toDoubleOrNull() ?: 0.0)+EXTRA_TAX_PERCENTAGE))) {
            return false
        }
        return true
    }

    /**
     * Check for valid tip amount
     * If entered tip amount is greater than PreAmount + Tax amount, then it will give error
     * @return Boolean
     */
    private fun validTipAmount(tip: String): Boolean {
        if (!changeTax &&
                (tip.toDoubleOrNull()
                        ?: 0.0) > ((transaction?.transactionAmount?.toDoubleOrNull() ?: 0.0) -
                        if (transaction?.reasonId.isNullOrEmpty()) {
                            transaction?.taxAmount?.toDoubleOrNull() ?: 0.0
                        } else {
                            transaction?.changedTaxAmount?.toDoubleOrNull() ?: 0.0
                        }
                        )) {
            return false
        }
        return true
    }

    /**
     * Check for valid tip amount.
     * After adding tip amount, pre total amount can not be zero
     * @return Boolean
     */
    private fun validPreTotalAmount(tip: String): Boolean {
        if (getPreTotalAmount(tip) <= 0) {
            return false
        }
        return true
    }

    private fun getPreTotalAmount(tip: String): Double {
        return if (changeTax) {
            ((transaction?.transactionAmount?.toDoubleOrNull()
                    ?: 0.0) - (transaction?.tipAmount?.toDoubleOrNull()
                    ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))
        } else {
            if(transaction?.reasonId.isNullOrEmpty()){
                ((transaction?.transactionAmount?.toDoubleOrNull()
                        ?: 0.0) - (transaction?.taxAmount?.toDoubleOrNull()
                        ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))
            }else{
                ((transaction?.transactionAmount?.toDoubleOrNull()
                        ?: 0.0) - (transaction?.changedTaxAmount?.toDoubleOrNull()
                        ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))
            }

        }
    }

    fun getTexAmountToShow(): String {

        if (transaction?.reasonId.isNullOrEmpty()) {
            if ((transaction?.taxAmount?.isNotEmpty() == true)
                    && (transaction?.taxAmount?.toDoubleOrNull() ?: 0.0 > 0.0)) {
                return transaction?.parseTaxAmount() ?: ""
            } else {
                return ""
            }
        } else {
            if ((transaction?.changedTaxAmount?.isNotEmpty() == true)
                    && (transaction?.changedTaxAmount?.toDoubleOrNull() ?: 0.0 > 0.0)) {
                return transaction?.parseTaxAmountDetail() ?: ""
            } else {
                return ""
            }
        }
    }

}