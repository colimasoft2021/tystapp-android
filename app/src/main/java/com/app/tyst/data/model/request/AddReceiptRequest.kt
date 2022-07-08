package com.app.tyst.data.model.request

data class AddReceiptRequest(
        var category: String = "",
        var storeName: String = "",
        var location: String = "",
        var city: String = "",
        var stateId: String = "",
        var zipCode: String = "",
        var amount: String = "",
        var transactionDate: String = "",
        var taxAmount: String = "",
        var receiptImage: String = "",
        var latitude: String = "",
        var longitude: String = ""
)