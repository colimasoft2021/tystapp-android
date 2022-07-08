package com.app.tyst.ui.myprofile

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.databinding.ListItemAddBankAccountBinding
import com.app.tyst.databinding.ListItemBankAccountsBinding
import com.app.tyst.ui.BaseViewHolder
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.hb.logger.Logger

/**
 * This adapter is used to show list of user's added bank accounts
 */
class BanksAdapter(private val listener: BankAdapterCallbacks?) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    private var adapterDataList: ArrayList<PlaidInstitutionResponse> = ArrayList()

    companion object {
        private const val TYPE_ADD_ACCOUNT = 0
        private const val TYPE_BANK_ACCOUNT = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD_ACCOUNT) {
            val itemBinding = ListItemAddBankAccountBinding.inflate(layoutInflater, parent, false)
            AddBankAccountViewHolder(itemBinding)
        } else {
            val itemBinding = ListItemBankAccountsBinding.inflate(layoutInflater, parent, false)
            BankAccountViewHolder(itemBinding)
        }
    }

    override fun getItemCount(): Int {
        return adapterDataList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is AddBankAccountViewHolder -> holder.bind(element)
            is BankAccountViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            TYPE_ADD_ACCOUNT
        else
            TYPE_BANK_ACCOUNT
    }

    fun setAccountList(accounts: ArrayList<PlaidInstitutionResponse>) {
        adapterDataList.clear()
        adapterDataList.addAll(accounts)
        notifyDataSetChanged()
    }

    /**
     * Remove deleted bank account from bank list
     * @param accounts PlaidInstitutionResponse Deleted bank account
     */
    fun removeBankAccount(accounts: PlaidInstitutionResponse?) {
        val index = adapterDataList.indexOf(accounts)
        if (index >= 0) {
            adapterDataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getBankAccount() = adapterDataList

    //----------------AddBankAccountViewHolder------------
    inner class AddBankAccountViewHolder(val binding: ListItemAddBankAccountBinding) : BaseViewHolder<PlaidInstitutionResponse>(binding) {

        override fun bind(item: PlaidInstitutionResponse) {
            binding.relativeLayout.clickWithDebounce {
                listener?.onAddAccountClick()
            }
        }
    }

    //----------------BankAccountViewHolder------------
    inner class BankAccountViewHolder(val binding: ListItemBankAccountsBinding) : BaseViewHolder<PlaidInstitutionResponse>(binding) {
        override fun bind(item: PlaidInstitutionResponse) {
            if (item.institutionLogo != null) {
                val decodedString = Base64.decode(item.institutionLogo, Base64.DEFAULT)
                binding.ivLogo.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size))
            }
            binding.tvBankAccountName.text = item.institutionName
            binding.btnDelete.makeBounceable()
            binding.btnDelete.clickWithDebounce { listener?.onBankAccountClick(item) }
        }
    }
}