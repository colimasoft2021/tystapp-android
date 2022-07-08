package com.app.tyst.ui.transactions.changetaxandtip

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.data.model.response.ReasonListResponse
import com.app.tyst.data.model.response.TransactionDetailResponse
import com.app.tyst.databinding.LayoutChangeTaxBottomSheetBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.focusOnField
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.validation.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * This bottom sheet dialog allow user to enter new tax or new tip value
 * @property listener DialogCreatedListener?
 * @property binding LayoutChangeTaxBottomSheetBinding
 */
class ChangeTaxBottomSheetDialog : BottomSheetDialogFragment() {
    interface ChangeValueListener {
        fun onValueChange(forChangeTax: Boolean, newValue: String, reason: ReasonListResponse?)
    }


    private var listener: ChangeValueListener? = null

    private lateinit var binding: LayoutChangeTaxBottomSheetBinding

    private var spinnerClick = false
    private var selectedReason: ReasonListResponse? = null

    private val viewModel: ChangeTaxViewModel by lazy {
        ViewModelProviders.of(this@ChangeTaxBottomSheetDialog).get(ChangeTaxViewModel::class.java)
    }

    companion object {

        /**
         * Create new instance.
         * @return ChangeTaxBottomSheetDialog
         */
        fun newInstance(changeTax: Boolean, transaction: TransactionDetailResponse): ChangeTaxBottomSheetDialog {
            val fragment = ChangeTaxBottomSheetDialog()
            val args = Bundle()
            args.putBoolean("change_tax", changeTax)
            args.putParcelable("transaction", transaction)
            fragment.arguments = args
            return fragment
        }
    }

    fun setListener(listener: ChangeValueListener) {
        this@ChangeTaxBottomSheetDialog.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.changeTax = arguments?.getBoolean("change_tax", true)
                ?: true
        viewModel.transaction = arguments?.getParcelable("transaction")
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_change_tax_bottom_sheet, null, false)
        dialog.setContentView(binding.root)
        initView()
        addObserver()
        if (viewModel.changeTax) {
            (activity as BaseActivity).showHideProgressDialog(true)
            viewModel.callCategoryList()
        }
    }

    @SuppressLint("CheckResult")
    private fun initView() {
        if (viewModel.changeTax) {
            binding.tvTitle.text = getString(R.string.change_tax)
            binding.btnChangeTax.text = getString(R.string.change_tax)
            binding.inputTaxAmount.hint = getString(R.string.hint_tax_amount)
            binding.flReason.visibility = View.VISIBLE

            val taxAmountStr = viewModel.getTexAmountToShow()
            if(taxAmountStr.isNotEmpty()){
                binding.inputTaxAmount.setText(taxAmountStr)
            }

//            if ((viewModel.transaction?.taxAmount?.isNotEmpty() == true)
//                    && (viewModel.transaction?.taxAmount?.toDoubleOrNull() ?: 0.0 > 0.0))
//                binding.inputTaxAmount.setText(viewModel.transaction?.parseTaxAmount())
        } else {
            binding.tvTitle.text = getString(R.string.add_tip)
            binding.btnChangeTax.text = getString(R.string.add_tip)
            binding.flReason.visibility = View.GONE
            binding.inputTaxAmount.hint = getString(R.string.hint_tip_amount)
            if ((viewModel.transaction?.tipAmount
                            ?.isNotEmpty() == true) && (viewModel.transaction?.tipAmount?.toDoubleOrNull() ?: 0.0 > 0.0))
                binding.inputTaxAmount.setText(viewModel.transaction?.parseTipAmount())
        }

        binding.btnChangeTax.makeBounceable()
        binding.btnChangeTax.clickWithDebounce {
            if (viewModel.isValid(binding.inputTaxAmount.getNumericValue()?.toString()
                            ?: "", selectedReason?.reasonId ?: "")) {
                listener?.onValueChange(viewModel.changeTax, binding.inputTaxAmount.getNumericValue()?.toString()
                        ?: "", selectedReason)
                this@ChangeTaxBottomSheetDialog.dismissAllowingStateLoss()
            }
        }

        binding.inputReasonText.clickWithDebounce {
            if (viewModel.getReasonList().isEmpty()) {
                spinnerClick = true
                (activity as BaseActivity).showHideProgressDialog(true)
                viewModel.callCategoryList()
            } else {
                binding.spReason.performClick()
            }
        }

        RxTextView.textChanges(binding.inputTaxAmount)
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it.toString() != "$")
                        binding.inputLayoutTaxAmount.error = null
                }

    }

    private fun addObserver() {
        viewModel.validationObserver.observe(this@ChangeTaxBottomSheetDialog, Observer {
            binding.root.focusOnField(it.failedViewId)
            when (it.failType) {
                EMPTY_TAX_AMOUNT -> {
                    binding.inputLayoutTaxAmount.error = getString(R.string.alert_enter_tax)
                }
                EMPTY_TIP_AMOUNT -> {
                    binding.inputLayoutTaxAmount.error = getString(R.string.alert_enter_tip)
                }
                EMPTY_REASON -> {
                    binding.inputLayoutReason.error = getString(R.string.alert_enter_reason_for_tax)
                }
                INVALID_TAX_AMOUNT_LIMIT -> {
                    val maxTax = (viewModel.transaction?.getTaxPercentageInNumber()?:0.0) + IConstants.EXTRA_TAX_PERCENTAGE
                    binding.inputLayoutTaxAmount.error = String.format(getString(R.string.alert_tax_percentage), maxTax.toString())+"%."
                }
                INVALID_TAX_AMOUNT -> {
                    binding.inputLayoutTaxAmount.error = getString(R.string.alert_tax_amount_greater_than_total)
                }
                INVALID_TIP_AMOUNT -> {
                    binding.inputLayoutTaxAmount.error = getString(R.string.alert_tip_amount_greater_total)
                }
                INVALID_TIP_AMOUNT_LIMIT -> {
                    binding.inputLayoutTaxAmount.error = String.format(getString(R.string.alert_tip_percentage), viewModel.transaction?.tipperc)+"%."
                }
                INVALID_PRE_TOTAL_AMOUNT -> {
                    binding.inputLayoutTaxAmount.error = getString(R.string.alert_pre_total_zero)
                }
            }
        })

        viewModel.reasonData.observe(this@ChangeTaxBottomSheetDialog, Observer {
            (activity as BaseActivity).showHideProgressDialog(false)
            if (it.settings?.isSuccess == true && it.data != null) {
                initSpinners(it.data ?: ArrayList())
            } else {
                it?.settings?.message?.showSnackBar(activity)
            }
        })
    }

    private fun initSpinners(reasons: ArrayList<ReasonListResponse>) {
        viewModel.getReasonList().clear()
        viewModel.getReasonList().addAll(reasons)
        binding.apply {
            spReason.adapter = ArrayAdapter<ReasonListResponse>(activity!!, R.layout.item_spinner,
                    android.R.id.text1,
                    viewModel.getReasonList().apply { add(0, ReasonListResponse(reason = getString(R.string.hint_select_reason))) })
            spReason.onItemSelectedListener = reasonItemListener
            if (spinnerClick)
                binding.spReason.performClick()
            spinnerClick = false
        }

        if(viewModel.transaction?.reasonId?.isNotEmpty() == true){
            val index = reasons.indexOfFirst {it.reasonId.equals(viewModel.transaction?.reasonId) }
            binding.spReason.setSelection(index+1)
        }
    }

    /**
     * Item listener for select reason
     */
    private val reasonItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            binding.inputReasonText.setText(binding.spReason.adapter.getItem(position).toString())
            binding.inputLayoutReason.error = null
            selectedReason = binding.spReason.adapter.getItem(position) as ReasonListResponse
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}