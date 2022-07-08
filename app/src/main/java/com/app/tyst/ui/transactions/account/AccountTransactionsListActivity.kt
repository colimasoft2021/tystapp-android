package com.app.tyst.ui.transactions.account

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.AccountTransactionsDataResponse
import com.app.tyst.data.model.response.InstitutionsTotalAmountItem
import com.app.tyst.data.model.response.TotalAmountItem
import com.app.tyst.data.model.response.TransactionDataResponse
import com.app.tyst.databinding.ActivityAccountTransactionsListBinding
import com.app.tyst.databinding.LayoutMessageDialogBinding
import com.app.tyst.databinding.ListItemTransactionBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.subaccounts.SubAccountsActivity
import com.app.tyst.ui.transactions.daterange.SelectDateRangeActivity
import com.app.tyst.ui.transactions.transactiondetail.TransactionDetailActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.fromServerDateToYYYYMMDD
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.setSwipeToRefreshColor
import com.jakewharton.rxbinding2.widget.RxTextView
import com.simpleadapter.SimpleAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


/**
 * This activity is used to show all transactions list of a bank account with total amount and tax he/she spend.
 */
class AccountTransactionsListActivity : BaseActivity() {

    companion object {
        /**
         * Start intent to open AccountTransactionsListActivity
         * @param mContext Context
         * @param institution Institution Id /Account Id whose transactions list need to show
         * @param startDate start date from which transaction list
         * @param endDate  start date from which transaction list
         * @return Intent
         */
        fun getStartIntent(mContext: Context, institution: InstitutionsTotalAmountItem, startDate: String, endDate: String): Intent {
            return Intent(mContext, AccountTransactionsListActivity::class.java).apply {
                putExtra("institution", institution)
                putExtra("startDate", startDate)
                putExtra("endDate", endDate)
            }
        }
    }

    private val viewModel: AccountTransactionsListViewModel by lazy {
        ViewModelProviders.of(this@AccountTransactionsListActivity).get(AccountTransactionsListViewModel::class.java)
    }

    lateinit var binding: ActivityAccountTransactionsListBinding

    private lateinit var adapter: SimpleAdapter<TransactionDataResponse>

    private var observerActivated = false
    private var shouldInfoDialogShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@AccountTransactionsListActivity, R.layout.activity_account_transactions_list)
        initView()
    }

    private fun initView() {
        setFireBaseAnalyticsData("id-accounttransactions", "view_accounttransactions", "view_accounttransactions")
        setIntentData()
        initListener()
        initRecycleView()
        addObserver()
        showSelectedDateRange()
        showHideProgressDialog(true)
        viewModel.callTransactionData()
    }

    private fun initListener() {
        initSearch()
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnClear.makeBounceable()
            btnClear.clickWithDebounce {
                inputSearch.setText("")
                viewModel.searchKeyword = ""
                if (binding.progressView.visibility != View.VISIBLE)
                    binding.progressView.visibility = View.VISIBLE
                viewModel.callTransactionData()
            }

            tvDateRange.makeBounceable()
            tvDateRange.clickWithDebounce {
                startActivityForResult(Intent(this@AccountTransactionsListActivity, SelectDateRangeActivity::class.java), IConstants.REQUEST_DATE_RANGE)
            }

            if (isPaidByCash == false) {
                btnFilter.visibility = View.VISIBLE
                btnFilter.makeBounceable()
                btnFilter.clickWithDebounce {
                    startActivityForResult(SubAccountsActivity.getStartIntent(this@AccountTransactionsListActivity, institutionId = viewModel.institution?.institutionId
                            ?: "", accountId = viewModel.accountId), IConstants.REQUEST_ACCOUNT)
                }
            }else{
                btnFilter.visibility = View.GONE
            }
        }
    }

    /**
     * Initialize search functionality
     */
    @SuppressLint("CheckResult")
    private fun initSearch() {
        RxTextView.textChanges(binding.inputSearch)
                .debounce(500, TimeUnit.MILLISECONDS)
//                .skip(3)
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it.trim().isNotEmpty()) {
                        viewModel.searchKeyword = it.trim().toString()
                        viewModel.resetPagination()
                        viewModel.callTransactionData()
                        if (binding.progressView.visibility != View.VISIBLE)
                            binding.progressView.visibility = View.VISIBLE
                        binding.btnClear.visibility = View.INVISIBLE
                    } else {
                        viewModel.searchKeyword = ""
                        binding.btnClear.visibility = View.INVISIBLE
                    }
                }
    }

    /**
     * Initialize transactions recycle view and show bank transactions
     */
    private fun initRecycleView() {
        binding.rvInstitutions.layoutManager = LinearLayoutManager(this)
        adapter = SimpleAdapter.with<TransactionDataResponse, ListItemTransactionBinding>(R.layout.list_item_transaction) { _, model, binding ->
            binding.transaction = model
        }
        binding.rvInstitutions.adapter = adapter

        binding.srlTransaction.setSwipeToRefreshColor()
        binding.srlTransaction.setOnRefreshListener {
            if (isNetworkConnected()) {
                viewModel.resetPagination()
                viewModel.callTransactionData()
            } else {
                binding.srlTransaction.isRefreshing = false
            }
        }

        adapter.setClickableViews({ _, model, _ ->
            startActivity(TransactionDetailActivity.getStartIntent(
                    mContext = this@AccountTransactionsListActivity, transactionId = model.tTransactionId
                    ?: ""))
        }, R.id.llMain)

        adapter.enableLoadMore(binding.rvInstitutions) {
            if (isNetworkConnected()&&viewModel.shouldLoadMore) {
                viewModel.callTransactionData()
                viewModel.shouldLoadMore = false
            }
            viewModel.shouldLoadMore
        }
    }

    /**
     * Set institutionId, start date and end date from selected account
     */
    private fun setIntentData() {
        viewModel.apply {
            institution = intent?.getParcelableExtra("institution") ?: InstitutionsTotalAmountItem()
            startDate = intent?.getStringExtra("startDate") ?: ""
            endDate = intent?.getStringExtra("endDate") ?: ""
            binding.institutionName = institution?.institutionName
            if (institution?.institutionId.isNullOrBlank()) {
                binding.title = getString(R.string.saved_receipts)
                binding.isPaidByCash = true
            } else {
                binding.title = getString(R.string.all_transactions)
                binding.isPaidByCash = false
            }
        }
    }

    fun addObserver() {
        viewModel.transactionLiveData.observe(this@AccountTransactionsListActivity, Observer {
            showHideProgressDialog(false)
            binding.srlTransaction.isRefreshing = false
            binding.progressView.visibility = View.GONE
            if (it.settings?.isSuccess == true) {
                setTransactionsData(it.data)
                adapter.setLoadMoreComplete()
                if (it.settings?.hasNextPage() == true) {
                    viewModel.pageIndex++
                    viewModel.shouldLoadMore = true
                }else{
                    viewModel.shouldLoadMore = false
                }
                if(shouldInfoDialogShow) {
                    shouldInfoDialogShow = false
                    showInformationDialog()
                }
            } else if (!handleApiError(it.settings)) {
                if (viewModel.pageIndex == 1) {
                    setTotalAmount(TotalAmountItem())
                    showHideNoData(true)
                }
            } else if (viewModel.pageIndex == 1) {
                setTotalAmount(TotalAmountItem())
            }

            if (viewModel.searchKeyword.isNotEmpty())
                binding.btnClear.visibility = View.VISIBLE
            observerActivated = true
        })

        (application as MainApplication).newReceiptAdded.observe(this@AccountTransactionsListActivity, Observer {
            if (it && observerActivated) {
                viewModel.resetPagination()
                showHideProgressDialog(true)
                viewModel.callTransactionData()
            }
        })

        (application as MainApplication).taxChanged.observe(this@AccountTransactionsListActivity, Observer {
            if (it && observerActivated) {
                observerActivated = false
                viewModel.resetPagination()
                showHideProgressDialog(true)
                viewModel.callTransactionData()
                observerActivated = true
            }
        })

        (application as MainApplication).categoryUpdated.observe(this@AccountTransactionsListActivity, Observer {
            Handler().postDelayed({
                if (it && observerActivated) {
                    observerActivated = false
                    viewModel.resetPagination()
                    showHideProgressDialog(true)
                    viewModel.callTransactionData()
                    observerActivated = true
                }
            },100)

        })
    }


    /**
     * Show total amount and tax applied according to institutions
     */
    private fun setTransactionsData(transactions: AccountTransactionsDataResponse?) {
        if (transactions?.transactionsData?.isNotEmpty() == true) {
            if (viewModel.pageIndex == 1) {
                showHideNoData(false)
                adapter.clear()
                adapter.addAll(transactions.transactionsData)
                adapter.notifyDataSetChanged()
            } else {
                val count = adapter.itemCount
                adapter.addAll(transactions.transactionsData)
                adapter.notifyItemRangeChanged(count, transactions.transactionsData.size)
            }
            adapter.setLoadMoreComplete()
        }

        if (viewModel.pageIndex == 1) {
            if (transactions?.totalAmount?.isNotEmpty() == true) {
                setTotalAmount(transactions.totalAmount[0])
            } else {
                setTotalAmount(TotalAmountItem())
            }
        }
    }

    /**
     * Set total amount and tax applied amount
     * @param totalAmount TotalAmountItem
     */
    private fun setTotalAmount(totalAmount: TotalAmountItem) {
        binding.bank = totalAmount
    }

    private fun showHideNoData(show: Boolean, message: String = getString(R.string.no_record_found)) {
        if (show) {
            binding.rvInstitutions.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvNoData.text = message
        } else {
            binding.rvInstitutions.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }

    private fun showSelectedDateRange() {
        binding.dateRange = viewModel.startDate.fromServerDateToYYYYMMDD() + " - " + viewModel.endDate.fromServerDateToYYYYMMDD()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // check if the requestCode is the wanted one and if the result is what we are expecting
            if (requestCode == IConstants.REQUEST_DATE_RANGE) {
                viewModel.resetPagination()
                viewModel.startDate = data?.getStringExtra("startDate") ?: ""
                viewModel.endDate = data?.getStringExtra("endDate") ?: ""
                showSelectedDateRange()
                showHideProgressDialog(true)
                viewModel.callTransactionData()
            } else if (requestCode == IConstants.REQUEST_ACCOUNT) {
                viewModel.resetPagination()
                viewModel.accountId = data?.getStringExtra("accountId") ?: ""
                binding.title = if (viewModel.accountId.isEmpty()) {
                    getString(R.string.all_transactions)
                } else {
                    data?.getStringExtra("accountName") ?: ""
                }
                binding.executePendingBindings()
                showHideProgressDialog(true)
                viewModel.callTransactionData()
            }
        }

    }

    /**
     * Show message to user for tax applied information.
     */
    private fun showInformationDialog() {
        showDialog()
    }

    private fun showDialog() {
        val binding = DataBindingUtil.inflate<LayoutMessageDialogBinding>(LayoutInflater.from(baseContext), R.layout.layout_message_dialog, null, false)
        val dialog = Dialog(this@AccountTransactionsListActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        binding.btnOk.clickWithDebounce {
            dialog.dismiss()
        }
        dialog.show()
    }
}
