package com.app.tyst.ui.home

import com.app.tyst.data.model.response.InstitutionsTotalAmountItem

interface HomeAdapterCallbacks {
    fun onAddAccountClick()
    fun onTransactionClick(account: InstitutionsTotalAmountItem)
    fun onAlertClick(account:InstitutionsTotalAmountItem)
}