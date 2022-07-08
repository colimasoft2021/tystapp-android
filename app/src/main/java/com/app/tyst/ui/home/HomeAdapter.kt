package com.app.tyst.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.data.model.response.InstitutionsTotalAmountItem
import com.app.tyst.databinding.ListItemAddBankAccountBinding
import com.app.tyst.databinding.ListItemInstitutionTransactionDetailBinding
import com.app.tyst.ui.BaseViewHolder
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.hb.logger.Logger

class HomeAdapter(private val listener: HomeAdapterCallbacks?) : RecyclerView.Adapter<BaseViewHolder<*>>() {
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    private var adapterDataList: ArrayList<InstitutionsTotalAmountItem> = ArrayList()

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
            val itemBinding = ListItemInstitutionTransactionDetailBinding.inflate(layoutInflater, parent, false)
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

    fun setTransactionList(accounts: ArrayList<InstitutionsTotalAmountItem>) {
        adapterDataList.clear()
        adapterDataList.addAll(accounts)
        notifyDataSetChanged()
    }

    /**
     * Remove deleted bank account from bank list
     * @param accounts PlaidInstitutionResponse Deleted bank account
     */
    fun removeBankAccount(accounts: InstitutionsTotalAmountItem?) {
        val index = adapterDataList.indexOf(accounts)
        if (index >= 0) {
            adapterDataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getBankAccount() = adapterDataList

    //----------------AddBankAccountViewHolder------------
    inner class AddBankAccountViewHolder(val binding: ListItemAddBankAccountBinding) : BaseViewHolder<InstitutionsTotalAmountItem>(binding) {

        override fun bind(item: InstitutionsTotalAmountItem) {
            binding.relativeLayout.clickWithDebounce {
                listener?.onAddAccountClick()
            }
        }
    }

    //----------------BankAccountViewHolder------------
    inner class BankAccountViewHolder(val binding: ListItemInstitutionTransactionDetailBinding) : BaseViewHolder<InstitutionsTotalAmountItem>(binding) {
        override fun bind(item: InstitutionsTotalAmountItem) {
            binding.institution = item
            binding.llMain.clickWithDebounce {
                listener?.onTransactionClick(item)
            }
            if(item.isError()){
                binding.btnAlert.visibility = View.VISIBLE
            }else{
                binding.btnAlert.visibility = View.GONE
            }
            binding.executePendingBindings()
            binding.btnAlert.makeBounceable()
            binding.btnAlert.clickWithDebounce {
                listener?.onAlertClick(item)
            }
        }
    }
}