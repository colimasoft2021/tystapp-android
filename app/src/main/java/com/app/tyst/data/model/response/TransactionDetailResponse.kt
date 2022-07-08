package com.app.tyst.data.model.response

import android.os.Parcelable
import com.app.tyst.utility.extension.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * This data class is used for parse transaction details data
 */
@Parcelize
data class TransactionDetailResponse(

        @field:SerializedName("transaction_date")
        val transactionDate: String? = null,

        @field:SerializedName("tax_amount")
        var taxAmount: String? = null,

        @field:SerializedName("transaction_id")
        val transactionId: String? = null,

        @field:SerializedName("t_transactions_id")
        val tTransactionsId: String? = null,

        @field:SerializedName("transaction_amount")
        val transactionAmount: String? = null,

        @field:SerializedName("store_name")
        val storeName: String? = null,

        @field:SerializedName("location")
        val location: String? = null,

        @field:SerializedName("tip_amount")
        var tipAmount: String? = null,

        @field:SerializedName("category")
        val category: String? = null,

        @field:SerializedName("receipt_images")
        val receiptImages: ArrayList<String> = ArrayList(),

        @field:SerializedName("reason")
        var reason: String? = null,

        @field:SerializedName("reason_id")
        var reasonId: String? = null,

        @field:SerializedName("taxperc")
        var taxperc: String? = null,

        @field:SerializedName("tip_perc")
        var tipperc: String? = null,

        @field:SerializedName("changed_tax_amount")
        var changedTaxAmount: String? = null


) : Parcelable {
    /**
     * Convert amount from String to Currency format
     * @return String
     */
    fun parseTransactionAmount() = if (transactionAmount.isNullOrEmpty()) "0.0" else transactionAmount.toCurrency()

    /**
     * Convert tax amount from String to Currency format
     * @return String
     */
    fun parseTaxAmount() = if (taxAmount.isNullOrEmpty()) "0.0" else taxAmount.toCurrency()

    /**
     * Convert tax amount from String to Currency format for transaction detail
     * @return String
     */
    fun parseTaxAmountDetail() = if(reasonId.isNullOrEmpty()){
        if (taxAmount.isNullOrEmpty()) "0.0" else taxAmount.toCurrency()
    }else{
        if (changedTaxAmount.isNullOrEmpty()) "0.0" else changedTaxAmount.toCurrency()
    }

    /**
     * Convert tax amount from String to Currency format
     * @return String
     */
    fun parseTipAmount() = if (tipAmount.isNullOrEmpty()) "0.0" else tipAmount.toCurrency()

    fun isLocationAvailable() = location?.isNotEmpty()

    fun parseTransactionDate() = transactionDate?.fromServerDateToYYYYMMDD()

    fun getTransactionNumber() = if (transactionId.isNullOrEmpty()) "" else "Transaction Number: $transactionId"

    /**
     * Get pre-total amount
     * @return String return pre total amount with currency symbol
     */
    fun getPreTotalAmount() = ((transactionAmount?.toDoubleOrNull()
            ?: 0.0) - (taxAmount?.toDoubleOrNull()
            ?: 0.0) - (tipAmount?.toDoubleOrNull() ?: 0.0)).toCurrency()

    /**
     * Get pre-total amount
     * @return String return pre total amount with currency symbol
     */
    fun getPreTotalAmountDetail() = if(reasonId.isNullOrEmpty()){ ((transactionAmount?.toDoubleOrNull()
            ?: 0.0) - (taxAmount?.toDoubleOrNull()
            ?: 0.0) - (tipAmount?.toDoubleOrNull() ?: 0.0)).toCurrency()
    }else{
        ((transactionAmount?.toDoubleOrNull()
                ?: 0.0) - (changedTaxAmount?.toDoubleOrNull()
                ?: 0.0) - (tipAmount?.toDoubleOrNull() ?: 0.0)).toCurrency()
    }

    /**
     * Get Pre total amount
     * @param tip String Tip amount
     * @return Double return pre total
     */
    fun getPreTotalAmount(tip:String) = ((transactionAmount?.toDoubleOrNull()
            ?: 0.0) - (taxAmount?.toDoubleOrNull()
            ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))

    /**
     * Get Pre total amount
     * @param tip String Tip amount
     * @return Double return pre total
     */
    fun getPreTotalAmountDetail(tip:String) =
    if(reasonId.isNullOrEmpty()){
        ((transactionAmount?.toDoubleOrNull()
                ?: 0.0) - (taxAmount?.toDoubleOrNull()
                ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))
    }else{
        ((transactionAmount?.toDoubleOrNull()
                ?: 0.0) - (changedTaxAmount?.toDoubleOrNull()
                ?: 0.0) - (tip.toDoubleOrNull() ?: 0.0))
    }



    fun getTotalAmount() = ((transactionAmount?.toDoubleOrNull()
            ?: 0.0) + (taxAmount?.toDoubleOrNull()
            ?: 0.0) + (tipAmount?.toDoubleOrNull() ?: 0.0)).toCurrency()

    fun isReceiptImageAvailable() = receiptImages.size > 0

    fun isTransactionIdAvailable() = transactionId?.isNotEmpty()

    /**
     * Calculate tax percentage on pre-total amount with tax amount
     * @return String
     */
    fun getTaxPercentage(): String {
        return (taxAmount?.toDoubleOrNull()
                ?: 0.0).whatPercentageOf(getPreTotalAmount().parseCurrencyToDouble() + (taxAmount?.toDoubleOrNull()
                ?: 0.0)).uptoTwoDecimal() + "%"
    }

    /**
     * Calculate tax percentage on pre-total amount with tax amount for transaction detail
     * @return String
     */
    fun getTaxPercentageDetail(): String {
        return  if(reasonId.isNullOrEmpty()){
            (taxAmount?.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(getPreTotalAmount().parseCurrencyToDouble() + (taxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal() + "%"
        }else{
            (changedTaxAmount?.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(getPreTotalAmountDetail().parseCurrencyToDouble() + (changedTaxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal() + "%"
        }

    }

    /**
     * Calculate tax percentage on pre-total amount with tax amount
     * @param tax String tax amount for which tax will be calculate
     * @return String
     */
    fun getTaxPercentage(tax:String): String {
        return (tax.toDoubleOrNull()
                ?: 0.0).whatPercentageOf(getPreTotalAmount().parseCurrencyToDouble() + (taxAmount?.toDoubleOrNull()
                ?: 0.0)).uptoTwoDecimal()
    }

    /**
     * Calculate tax percentage on pre-total amount with tax amount
     * @param tax String tax amount for which tax will be calculate
     * @return String
     */
    fun getTaxPercentageDetail(tax:String): String {
        return  if(reasonId.isNullOrEmpty()){
            (tax.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(getPreTotalAmount().parseCurrencyToDouble() + (taxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal()
        }else{
            (tax.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(getPreTotalAmountDetail().parseCurrencyToDouble() + (changedTaxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal()

        }
    }

    /**
     * Calculate tax percentage on given pre-total amount with tax amount
     * @param preTotal Double pre-total on which tax percentage will calculate
     * @return String
     */
    fun getTaxPercOnPreTotal(preTotal:Double): String {
        return (taxAmount?.toDoubleOrNull()
                ?: 0.0).whatPercentageOf(preTotal + (taxAmount?.toDoubleOrNull()
                ?: 0.0)).uptoTwoDecimal()
    }

    /**
     * Calculate tax percentage on given pre-total amount with tax amount
     * @param preTotal Double pre-total on which tax percentage will calculate
     * @return String
     */
    fun getTaxPercOnPreTotalDetail(preTotal:Double): String {
        return if(reasonId.isNullOrEmpty()) {
            (taxAmount?.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(preTotal + (taxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal()
        }else{
            (changedTaxAmount?.toDoubleOrNull()
                    ?: 0.0).whatPercentageOf(preTotal + (changedTaxAmount?.toDoubleOrNull()
                    ?: 0.0)).uptoTwoDecimal()
        }
    }

    /**
     * Calculate tip percentage on total amount
     * @param tip String
     * @return String
     */
    fun getTipPercentage(tip:String): String {
        return (tip.toDoubleOrNull()
                ?: 0.0).whatPercentageOf(transactionAmount?.toDoubleOrNull()?:0.0).uptoTwoDecimal()
    }

    fun getChangeTaxReason() = "Tax Change Reason: $reason"

    fun isReasonAvailable() = !reason.isNullOrEmpty()

    fun getOriginalTaxPercentage() = if(taxperc.isNullOrEmpty()) "" else "Original Tax(%) : $taxperc%"

    fun isOriginalTaxAvailable() = taxperc?.isNotEmpty()

    fun getTaxPercentageInNumber():Double{
        return taxperc?.toDouble()?:0.0
    }
}