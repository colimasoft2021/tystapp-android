package com.app.tyst.ui.subaccounts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.tyst.R
import com.app.tyst.data.model.response.AccountResponse
import com.app.tyst.databinding.ActivitySubAccountsBinding
import com.app.tyst.databinding.ListItemAccountsBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.setSwipeToRefreshColor
import com.jakewharton.rxbinding2.widget.RxTextView
import com.simpleadapter.SimpleAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SubAccountsActivity : BaseActivity() {

    companion object {
        /**
         * Start intent to open SubAccountsActivity
         * @param mContext Context
         * @param accountId Account Id
         * @return Intent
         */
        fun getStartIntent(mContext: Context, institutionId: String, accountId: String): Intent {
            return Intent(mContext, SubAccountsActivity::class.java).apply {
                putExtra("institutionId", institutionId)
                putExtra("accountId", accountId)
            }
        }
    }

    lateinit var binding: ActivitySubAccountsBinding

    private lateinit var adapter: SimpleAdapter<AccountResponse>

    private val viewModel: SubAccountsViewModel by lazy {
        ViewModelProviders.of(this@SubAccountsActivity).get(SubAccountsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SubAccountsActivity, R.layout.activity_sub_accounts)
        initView()
    }

    private fun initView() {
        initSearch()
        initListener()
        initRecycleView()
        addObserver()
        viewModel.callUserAccounts(intent?.getStringExtra("institutionId") ?: "")
        showHideProgressDialog(true)
    }

    private fun initRecycleView() {
        binding.rvAccounts.layoutManager = LinearLayoutManager(this)
        adapter = SimpleAdapter.with<AccountResponse, ListItemAccountsBinding>(R.layout.list_item_accounts) { _, model, binding ->
            binding.accounts = model
            binding.executePendingBindings()
        }
        binding.rvAccounts.adapter = adapter

        binding.srlAccounts.setSwipeToRefreshColor()
        binding.srlAccounts.setOnRefreshListener {
            if (isNetworkConnected()) {
                viewModel.callUserAccounts(intent?.getStringExtra("institutionId") ?: "")
            } else {
                binding.srlAccounts.isRefreshing = false
            }
        }

        adapter.setClickableViews({ _, model, _ ->
            if(isNetworkConnected()) {
                sendResult(model.accountId ?: "", model.accountName ?: "")
            }
        }, R.id.llMain)
    }

    private fun initListener() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce {
                finish()
            }

            btnReset.makeBounceable()
            btnReset.clickWithDebounce {
                if(isNetworkConnected()) {
                    sendResult("", "")
                }
            }
        }
    }

    private fun addObserver() {
        viewModel.accountsData.observe(this@SubAccountsActivity, Observer {
            showHideProgressDialog(false)
            binding.srlAccounts.isRefreshing = false
            if (it.settings?.isSuccess == true) {
                setAccountsData(it.data)
            } else if (!handleApiError(it.settings)) {
                showHideNoData(true)
            } else {
                showHideNoData(true)
            }

//            if (viewModel.searchKeyword.isNotEmpty())
//                binding.btnClear.visibility = View.VISIBLE
        })
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
                        if (binding.progressView.visibility != View.VISIBLE)
                            binding.progressView.visibility = View.VISIBLE
                        binding.btnClear.visibility = View.INVISIBLE
                    } else {
                        binding.btnClear.visibility = View.INVISIBLE
                    }
                }
    }

    /**
     * Show total amount and tax applied according to institutions
     */
    private fun setAccountsData(accounts: ArrayList<AccountResponse>?) {
        if (accounts?.isNotEmpty() == true) {
            showHideNoData(false)
            accounts.find { it.accountId == intent?.getStringExtra("accountId") ?: "" }?.isSelected = true
            adapter.clear()
            adapter.addAll(accounts)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showHideNoData(show: Boolean, message: String = getString(R.string.no_accounts_found)) {
        if (show) {
            binding.rvAccounts.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvNoData.text = message
        } else {
            binding.rvAccounts.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }

    /**
     * Send selected account to calling activity
     */
    private fun sendResult(accountId: String, accountName: String) {
        // if you want to send any type of data back, such as an object, string, int etc
        // you have to create an intent and use this bit of code
        // otherwise you can just use setResult(Activity.RESULT_OK) and finish()
        try {
            val resultIntent = Intent()
            resultIntent.putExtra("accountId", accountId)
            resultIntent.putExtra("accountName", accountName)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } catch (ex: Exception) {

            logger.dumpCrashEvent(ex)
        }
    }

}
