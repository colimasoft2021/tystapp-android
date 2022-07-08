package com.app.tyst.data.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AccountResponse(

	@field:SerializedName("balance_limit")
	val balanceLimit: String? = null,

	@field:SerializedName("account_type")
	val accountType: String? = null,

	@field:SerializedName("balance_unofficial_currency_code")
	val balanceUnofficialCurrencyCode: String? = null,

	@field:SerializedName("balance_available")
	val balanceAvailable: String? = null,

	@field:SerializedName("account_subtype")
	val accountSubtype: String? = null,

	@field:SerializedName("p_account_id")
	val pAccountId: String? = null,

	@field:SerializedName("institution_id")
	val institutionId: String? = null,

	@field:SerializedName("balance_iso_currency_code")
	val balanceIsoCurrencyCode: String? = null,

	@field:SerializedName("account_id")
	val accountId: String? = null,

	@field:SerializedName("balance_current")
	val balanceCurrent: String? = null,

	@field:SerializedName("account_mask")
	val accountMask: String? = null,

	@field:SerializedName("account_official_name")
	val accountOfficialName: String? = null,

	@field:SerializedName("account_name")
	val accountName: String? = null,

	var isSelected:Boolean = false
) : Parcelable