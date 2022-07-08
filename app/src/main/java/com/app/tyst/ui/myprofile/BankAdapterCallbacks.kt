package com.app.tyst.ui.myprofile

import com.app.tyst.data.model.response.PlaidInstitutionResponse

interface BankAdapterCallbacks {
    fun onAddAccountClick()
    fun onBankAccountClick(account: PlaidInstitutionResponse)
}