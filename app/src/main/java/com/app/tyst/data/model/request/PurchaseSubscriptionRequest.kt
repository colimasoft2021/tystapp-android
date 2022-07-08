package com.app.tyst.data.model.request

data class PurchaseSubscriptionRequest(
        var receiptType: String = "",
        var subscriptionId: String = "",
        var purchaseToken: String = "",
        var purchaseJason: String = ""
)