package com.app.tyst.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.InstitutionsTotalAmountItem
import com.app.tyst.data.model.response.InstitutionsTransactionResponse
import com.app.tyst.data.model.response.TotalAmountItem
import com.app.tyst.databinding.FragmantHomeBinding
import com.app.tyst.ui.core.BaseFragment
import com.app.tyst.ui.settings.subscription.SubscriptionActivity
import com.app.tyst.ui.transactions.account.AccountTransactionsListActivity
import com.app.tyst.ui.transactions.daterange.SelectDateRangeActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.*
import com.app.tyst.utility.helper.LOGApp
import com.plaid.link.Plaid
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler

//import com.plaid.linkbase.models.LinkCancellation
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.LinkEventListener
//import com.plaid.linkbase.models.PlaidApiError


class HomeFragment : BaseFragment<FragmantHomeBinding>(), HomeAdapterCallbacks {

    private lateinit var binding: FragmantHomeBinding

    //    private lateinit var adapter: SimpleAdapter<InstitutionsTotalAmountItem>
    private lateinit var adapter: HomeAdapter
    private var errorInstitutionId = ""
    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this@HomeFragment).get(HomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentFragment(this)
    }


    override fun getLayoutId(): Int {
        return R.layout.fragmant_home
    }

    override fun iniViews() {
        binding = getViewDataBinding()
        mBaseActivity?.showAdMob(binding.adView)
        mBaseActivity?.setFireBaseAnalyticsData("id-homescreen", "view_homescreen", "view_homescreen")
        showSelectedDateRange()
        initRecycleView()
        addObserver()
        Handler().postDelayed({
            if (mBaseActivity?.isNetworkConnected() == true) {
                if (sharedPreference.getInstitutionsList()?.isNotEmpty() == true &&
                        sharedPreference.getInstitutionsList()?.find { it.isFirstTransactionsDumped?.equals("1") == true } != null) {
                    // If there is any account whose transactions is not available on server, then get transaction of those accounts
                    getTransactionOfAccount()
                } else {
                    getAllTransactions()
                }
            }
            viewModel.callSubscriptionStatus()
        }, 500)

    }

    override fun initListener() {
        binding.tvDateRange.makeBounceable()
        binding.tvDateRange.clickWithDebounce {
            mBaseActivity?.setFireBaseAnalyticsData("id-datefilter", "click_datefilter", "click_datefilter")
            startActivityForResult(SelectDateRangeActivity.getStartIntent(mContext = mBaseActivity!!, showReset = true), IConstants.REQUEST_DATE_RANGE)
        }
    }

    /**
     * This function will fetch transaction of newly added account, whose transaction data is not stored in our server.
     * So we need to first fetch transaction of those accounts and save to our server
     */
    private fun getTransactionOfAccount() {
        mBaseActivity?.showHideProgressDialog(true, getString(R.string.msg_fetching_transactions))
        Handler().postDelayed({
            viewModel.callFetchTransactions(sharedPreference.getInstitutionsList()?.find { it.isFirstTransactionsDumped?.equals("1") == true }?.institutionId
                    ?: "")
        }, 5000)
    }

    /**
     * Get all transaction details of user for giving date range
     */
    private fun getAllTransactions() {
        mBaseActivity?.showHideProgressDialog(true)
        viewModel.callAllInstitutionsTransactions()
    }

    private fun showSelectedDateRange() {
        binding.dateRange = viewModel.startDate.fromServerDateToYYYYMMDD() + " - " + viewModel.endDate.fromServerDateToYYYYMMDD()
    }

    /**
     * Initialize bank accounts recycle view and show bank accounts
     */
    private fun initRecycleView() {
        binding.rvInstitutions.layoutManager = GridLayoutManager(mBaseActivity, 2, RecyclerView.VERTICAL, false)
//        adapter = SimpleAdapter.with<InstitutionsTotalAmountItem, ListItemInstitutionTransactionDetailBinding>(R.layout.list_item_institution_transaction_detail) { _, model, binding ->
//            binding.institution = model
//        }
        adapter = HomeAdapter(this@HomeFragment)
        binding.rvInstitutions.adapter = adapter

        binding.srlHome.setSwipeToRefreshColor()
        binding.srlHome.setOnRefreshListener {
            if (mBaseActivity?.isNetworkConnected() == true) {
                viewModel.callAllInstitutionsTransactions()
            } else {
                binding.srlHome.isRefreshing = false
            }

        }

    }

    fun addObserver() {
        // Listen user has purchased ad free and remove add from this screen
        (mBaseActivity?.application as MainApplication).isAdRemoved.observe(this@HomeFragment, Observer {
            Handler().postDelayed({
                viewModel.callAllInstitutionsTransactions()
            }, 500)

            if (it) {
                binding.adView.visibility = View.GONE
            } else {
                binding.adView.visibility = View.VISIBLE
                mBaseActivity?.showAdMob(binding.adView)
            }
        })

        viewModel.institutionsTransactionLiveData.observe(this, Observer {
            mBaseActivity?.showHideProgressDialog(false)
            binding.srlHome.isRefreshing = false
            if (it.settings?.isSuccess == true) {
                setTransactionsDetail(it.data)
            } else if (mBaseActivity?.handleApiError(it.settings) == false) {
                showPayByCash()
            }
        })

        viewModel.fetchTransactionData.observe(this, Observer {
            viewModel.callAllInstitutionsTransactions()
            when {
                it.settings?.isSuccess == true -> {
                    viewModel.markedAccountAsRecorded()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
//                    mBaseActivity?.showHideProgressDialog(false)
                    it.settings?.message?.showSnackBar(mBaseActivity)
                }
                else -> {
                    // mBaseActivity?.showHideProgressDialog(false)
                }
            }
        })

        getHomeActivity().newAccountAdded.observe(this@HomeFragment, Observer {
            if (it) {
                viewModel.callFetchTransactions(sharedPreference.getInstitutionsList()?.find { institution -> institution.isFirstTransactionsDumped?.equals("1") == true }?.institutionId
                        ?: "")
            }
        })

        (mBaseActivity?.application as MainApplication).taxChanged.observe(this@HomeFragment, Observer {
            if (it) {
                getAllTransactions()
            }
        })

        (mBaseActivity?.application as MainApplication).newReceiptAdded.observe(this@HomeFragment, Observer {
            if (it) {
                getAllTransactions()
            }
        })

//        getHomeActivity().newReceiptAdded.observe(this@HomeFragment, Observer {
//            if (it) {
//                viewModel.callAllInstitutionsTransactions()
//            }
//        })

        getHomeActivity().accountDeleted.observe(this@HomeFragment, Observer {
            if (it) {
                getAllTransactions()
            }
        })

        viewModel.subscriptionStatusData.observe(this@HomeFragment, Observer {
            if (it.settings?.isSuccess == true && it.data != null) {
                val user = sharedPreference.userDetail
                user?.purchaseStatus = it.data?.isSubscribed
                sharedPreference.userDetail = user
                (mBaseActivity?.application as MainApplication).isAdRemoved.value = user?.isSubscripbed()
            }
        })

        viewModel.addAccountLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            when {
                it.settings?.isSuccess == true -> {
                    viewModel.saveInstitutionDetail(it.data)
                    if (mBaseActivity is HomeActivity) {
                        notifyAccountAdded()
                    }
                    mBaseActivity?.showInterstitial()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    it?.settings?.message?.showSnackBar(mBaseActivity)
                }
            }
        })

        viewModel.publicTokenData.observe(this, Observer {
            mBaseActivity?.showHideProgressDialog(false)
            when {
                it.settings?.isSuccess == true -> {
                    errorInstitutionId = it.data?.userInstitutionId?:""
                    openPlaidSdk(it.data?.publicToken?:"")
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    it.settings?.message?.showSnackBar(mBaseActivity)
                }
            }
        })

        viewModel.institutionErrorResloveData.observe(this, Observer {
            mBaseActivity?.showHideProgressDialog(false)
            errorInstitutionId = ""
            when {
                it.settings?.isSuccess == true -> {
                    viewModel.callAllInstitutionsTransactions()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    it.settings?.message?.showSnackBar(mBaseActivity)
                }
            }
        })
    }

    private fun setTransactionsDetail(detail: InstitutionsTransactionResponse?) {
        if (!detail?.totalAmount.isNullOrEmpty())
            setTotal(detail!!.totalAmount[0])

        if (!detail?.institutionsTotalAmount.isNullOrEmpty()) {
            showHideNoData(false)
            setInstitutionsData(detail!!.institutionsTotalAmount)
        } else {
            setInstitutionsData(ArrayList())
//            showHideNoData(true)
        }
    }

    /**
     * Show total amount and tax applied
     */
    private fun setTotal(total: TotalAmountItem) {
        binding.apply {
            this.total = total
            executePendingBindings()
        }
    }

    /**
     * Show total amount and tax applied according to institutions
     */
    private fun setInstitutionsData(institutions: ArrayList<InstitutionsTotalAmountItem>) {
        adapter.setTransactionList(institutions.also {
            it.add(0, InstitutionsTotalAmountItem())
        })
    }

    /**
     * If there is no any accounts transaction data available, then by default PayByCash item will show with 0.0 amount
     */
    private fun showPayByCash() {

        setTransactionsDetail(InstitutionsTransactionResponse().apply {
            institutionsTotalAmount = arrayListOf(InstitutionsTotalAmountItem(taxAmount = "0.0",
                    transactionAmount = "0.0", accessToken = "",
                    institutionName = getString(R.string.paid_by_cash), institutionId = ""))

            totalAmount = arrayListOf(TotalAmountItem(totalTransactionAmount = "0.0", totalTaxAmount = "0.0"))
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IConstants.REQUEST_PLAID_LINK_CODE -> {
                if (!myPlaidLinkActivityResultHandler.onActivityResult(requestCode, resultCode, data)) {
                    logger.debugEvent("Plaid Response", "Not handled")
                }
            }
            IConstants.REQUEST_DATE_RANGE -> when (resultCode) {
                RESULT_OK -> {
                    if (data?.getBooleanExtra("isReset", false) == true) {
                        viewModel.resetDateRange()
                    } else {
                        viewModel.startDate = data?.getStringExtra("startDate") ?: ""
                        viewModel.endDate = data?.getStringExtra("endDate") ?: ""
                    }
                    showSelectedDateRange()
                    mBaseActivity?.showHideProgressDialog(true)
                    viewModel.callAllInstitutionsTransactions()
                }
            }
            else -> logger.warningEvent("Activity Result", "Activity result not handled for $requestCode")
        }
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

    private fun performAddAccountClick() {
        if (sharedPreference.userDetail?.isSubscripbed() == true) {
            if (mBaseActivity?.isNetworkConnected() == true)
                showAddAccountMessageDialog()
        } else {
            startActivity(Intent(mBaseActivity!!, SubscriptionActivity::class.java))
        }
    }

    private fun openPlaidSdk(publicToken: String = "") {
        Plaid.setLinkEventListener(linkEventListener = {
            LOGApp.e("Event", it.toString())
        })
        Plaid.openLink(
                activity as HomeActivity,
                if (publicToken.isNotEmpty()) {
                    viewModel.getLinkReAuthenticationConfiguration(publicToken)
                } else {
                    viewModel.getLinkConfiguration()
                },
                IConstants.REQUEST_PLAID_LINK_CODE
        )
    }

    private fun notifyAccountAdded() {
        mBaseActivity?.showHideProgressDialog(true, getString(R.string.msg_fetching_transactions))
        Handler().postDelayed({
            getHomeActivity().newAccountAdded.value = true
//            mBaseActivity?.showHideProgressDialog(false)
        }, 5000)
    }

    override fun onAddAccountClick() {
        performAddAccountClick()
    }

    override fun onTransactionClick(account: InstitutionsTotalAmountItem) {
        startActivity(AccountTransactionsListActivity.getStartIntent(
                mContext = mBaseActivity!!, institution = account, startDate = viewModel.startDate, endDate = viewModel.endDate))
    }

    override fun onAlertClick(account: InstitutionsTotalAmountItem) {
        openAlertMessage(account)
    }

    private val myPlaidLinkActivityResultHandler by lazy {
        PlaidLinkResultHandler(
                requestCode = IConstants.REQUEST_PLAID_LINK_CODE,
                onSuccess = {
                    var accountIds = ""
                    var accountNames = ""
                    var accountNumbers = ""
                    it.linkConnectionMetadata.accounts.forEachIndexed { index, linkAccount ->
                        if (index == 0) {
                            accountIds = linkAccount.accountId
                            accountNames = linkAccount.accountName ?: ""
                            accountNumbers = linkAccount.accountNumber ?: ""
                        } else {
                            accountIds += ", " + linkAccount.accountId
                            accountNames += ", " + linkAccount.accountName ?: ""
                            accountNumbers += ", " + linkAccount.accountNumber ?: ""
                        }

                    }

                    var info = "##Public Token: " + it.publicToken + "                   ##Account Ids:" + accountIds + "         ##Account Names: " + accountNames + "                   ##Account Numbers: " + accountNumbers + "          ##Institution Id: " + it.linkConnectionMetadata.institutionId + "         ##Institution Name: " + it.linkConnectionMetadata.institutionName
                    if (mBaseActivity!!.isNetworkConnected()) {
                        showHideProgressDialog(true)
                        if(errorInstitutionId.isNotEmpty()){
                            viewModel.callUpdateInstitutionErrorLog(institutionId = errorInstitutionId)
                        }else{
                            viewModel.callAddBankAccount(it)
                        }
                    }
                },
                onCancelled = {
                    logger.debugEvent("Plaid Cancelled", getString(
                            R.string.content_cancelled,
                            it.institutionId ?: "",
                            it.institutionName ?: "",
                            it.linkSessionId ?: "",
                            it.status ?: ""
                    ))
//                    it.institutionName ?: "" + " Failed after " +
//                    it.status?.showSnackBar(mBaseActivity)
                },
                onExit = {
                    logger.debugEvent("Plaid Exit", getString(
                            R.string.content_exit,
                            it.displayMessage ?: "",
                            it.errorCode ?: "",
                            it.errorMessage ?: "",
                            it.linkExitMetadata.institutionId ?: "",
                            it.linkExitMetadata.institutionName ?: "",
                            it.linkExitMetadata.status ?: ""
                    ))
                    "Plaid Exit with" + it.displayMessage.showSnackBar(mBaseActivity)
                }
        )
    }

    /**
     * Show alert dialog if there is any error in user's institution
     */
    private fun openAlertMessage(account: InstitutionsTotalAmountItem) {
        DialogUtil.alert(context = mBaseActivity!!, msg = account.errorMessage ?: "",
                positiveBtnText = if (account.isLoginRequired() == true) {
                    "Login"
                } else {
                    getString(R.string.ok)
                },
                negativeBtnText = if (account.isLoginRequired() == true) {
                    getString(R.string.no)
                } else {
                    ""
                },
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        if (account.isLoginRequired() == true) {
                             showHideProgressDialog(true)
                              viewModel.callGeneratePublicToken(account)
                        }
                    }

                    override fun onCancel(isNeutral: Boolean) {
                    }
                }, isCancelable = true)
    }

    /**
     * Show message to user before add bank account.
     */
    private fun showAddAccountMessageDialog() {
        DialogUtil.alert(context = mBaseActivity!!, msg = getString(R.string.msg_add_bank_account),
                positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
                il = object : DialogUtil.IL{
                    override fun onSuccess() {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Bank Account")
                        mBaseActivity?.setFireBaseAnalyticsData("id-addbank", "click_addbank", "click_addbank")
                        openPlaidSdk()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                    }

                }, isCancelable = true)
    }
}