package com.app.tyst.ui.myprofile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.PlaidInstitutionResponse
import com.app.tyst.databinding.FragmentMyProfileBinding
import com.app.tyst.ui.authentication.AddAccountRepository
import com.app.tyst.ui.core.BaseFragment
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.ui.settings.editprofile.EditProfileActivity
import com.app.tyst.ui.settings.subscription.SubscriptionActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.helper.LOGApp
import com.hb.logger.data.model.CustomLog
import com.plaid.link.Plaid
import com.plaid.linkbase.models.connection.LinkCancellation
import com.plaid.linkbase.models.connection.LinkConnection
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler

//import com.plaid.linkbase.models.LinkCancellation
//import com.plaid.linkbase.models.LinkConnection
//import com.plaid.linkbase.models.LinkEventListener
//import com.plaid.linkbase.models.PlaidApiError

class MyProfileFragment : BaseFragment<FragmentMyProfileBinding>(), BankAdapterCallbacks {


    lateinit var binding: FragmentMyProfileBinding
    private var adapter: BanksAdapter? = null
    private var deletedAccount: PlaidInstitutionResponse? = null
    private val viewModel: MyProfileViewModel by lazy {
        ViewModelProviders.of(this).get(MyProfileViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentFragment(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_profile
    }

    override fun iniViews() {
        binding = getViewDataBinding()
        mBaseActivity?.showAdMob(binding.adView)
        binding.user = sharedPreference.userDetail
        initRecycleView()
        addObserver()
        viewModel.callGetBankAccount()
    }

    override fun initListener() {
        binding.btnEditProfile.makeBounceable()
        binding.btnEditProfile.clickWithDebounce {
            startActivity(Intent(mBaseActivity, EditProfileActivity::class.java))
        }
    }

    /**
     * Initialize bank accounts recycle view and show bank accounts
     */
    private fun initRecycleView() {
        binding.rvAccounts.layoutManager = GridLayoutManager(mBaseActivity, 2, RecyclerView.VERTICAL, false)
        adapter = BanksAdapter(this@MyProfileFragment)
        binding.rvAccounts.adapter = adapter
        binding.srlAccounts.setOnRefreshListener {
            if (mBaseActivity?.isNetworkConnected() == true) {
                viewModel.callGetBankAccount()
            } else {
                binding.srlAccounts.isRefreshing = false
            }

        }

        showBankAccounts()
    }

    fun addObserver() {

        getHomeActivity().newAccountAdded.observe(this@MyProfileFragment, Observer {
            if (it) {
                viewModel.callGetBankAccount()
            }
        })


        (mBaseActivity?.application as MainApplication).isProfileUpdated.observe(this@MyProfileFragment, Observer {
            if (it) {
                binding.user = sharedPreference.userDetail
                binding.executePendingBindings()
            }
        })

        // Listen user has purchased ad free and remove add from this screen
        (mBaseActivity?.application as MainApplication).isAdRemoved.observe(this@MyProfileFragment, Observer {
            Handler().postDelayed({
                if (it) {
                    binding.adView.visibility = View.GONE
                    viewModel.callGetBankAccount()
                } else {
                    binding.adView.visibility = View.VISIBLE
                    viewModel.callGetBankAccount()
                }
            }, 500)

        })

        viewModel.addAccountLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            when {
                it.settings?.isSuccess == true -> {
                    viewModel.saveInstitutionDetail(it.data)
                    showBankAccounts()
                    if (mBaseActivity is HomeActivity) {
                        notifyAccountAdded()
                    }
                    mIdlingResource?.setIdleState(true)
                    mBaseActivity?.showInterstitial()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    it?.settings?.message?.showSnackBar(mBaseActivity)
                    mIdlingResource?.setIdleState(true)
                }
                else -> mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.deleteAccountLiveData.observe(this@MyProfileFragment, Observer {
            showHideProgressDialog(false)
            when {
                it.settings?.isSuccess == true -> {
                    it?.settings?.message?.showSnackBar(mBaseActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                    adapter?.removeBankAccount(deletedAccount)
                    viewModel.saveBankListAfterRemoveBankAccount(adapter?.getBankAccount())
                    getHomeActivity().accountDeleted.value = true
                    mIdlingResource?.setIdleState(true)
                    mBaseActivity?.showInterstitial()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    it?.settings?.message?.showSnackBar(mBaseActivity)
                    mIdlingResource?.setIdleState(true)
                }
                else -> mIdlingResource?.setIdleState(true)
            }
        })

        viewModel.accountsLiveData.observe(this@MyProfileFragment, Observer {
            showHideProgressDialog(false)
            binding.srlAccounts.isRefreshing = false
            when {
                it.settings?.isSuccess == true -> {
                    setAccountsData(it.data)
                    mIdlingResource?.setIdleState(true)
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
//                    it?.settings?.message?.showSnackBar(mBaseActivity)
                    if (it.settings?.success?.equals("0") == true)
                        setAccountsData(it.data)
                    mIdlingResource?.setIdleState(true)
                }
                else -> mIdlingResource?.setIdleState(true)
            }
        })
    }

    private fun setAccountsData(accounts: ArrayList<PlaidInstitutionResponse>?) {
        viewModel.saveInstitutionDetail(accounts)
        showBankAccounts()
        if ((accounts?.find { account -> account.isFirstTransactionsDumped?.equals("1", true) == true } != null)
                && (mBaseActivity is HomeActivity)) {
            notifyAccountAdded()
        }
    }

    private fun notifyAccountAdded() {
        Handler().postDelayed({
            getHomeActivity().newAccountAdded.value = true
        }, 5000)
    }

    /**
     * Show added bank accounts list
     */
    private fun showBankAccounts() {
//        showHideNoData((sharedPreference.getInstitutionsList()?.toList() ?: ArrayList()).isEmpty())
        adapter?.setAccountList((sharedPreference.getInstitutionsList() ?: ArrayList()).also {
            it.add(0, PlaidInstitutionResponse())
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!myPlaidLinkActivityResultHandler.onActivityResult(requestCode, resultCode, data)) {
            logger.debugEvent("Plaid Response", "Not handled")
        }
    }

    private fun openPlaidSdk() {
        Plaid.setLinkEventListener(linkEventListener = {
            LOGApp.e("Event", it.toString())
        })
        Plaid.openLink(
                activity as HomeActivity,
                viewModel.getLinkConfiguration(),
                IConstants.REQUEST_PLAID_LINK_CODE
        )
    }

    private fun showHideNoData(show: Boolean) {
        if (show) {
            binding.rvAccounts.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.rvAccounts.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }


    override fun onAddAccountClick() {
        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Bank Account")
        mBaseActivity?.setFireBaseAnalyticsData("id-addbank", "click_addbank", "click_addbank")

//        val map = HashMap<String, String>()
//        map["institution_id"] = "ins_117003"
//        map["public_token"] = "public-development-7b992da2-e5a2-42a2-9373-31856ae6fc61"
//        map["institution_name"] = "Police and Fire Federal Credit Union"
//        viewModel.callAddBankAccount(map)
        if (sharedPreference.userDetail?.isSubscripbed() == true) {
            if (mBaseActivity?.isNetworkConnected() == true)
                showAddAccountMessageDialog()
        } else {
            startActivity(Intent(mBaseActivity!!, SubscriptionActivity::class.java))
        }
    }

    override fun onBankAccountClick(account: PlaidInstitutionResponse) {
        mBaseActivity?.setFireBaseAnalyticsData("id-deletebank", "click_deletebank", "click_deletebank")
        performUnlinkBankAccount(account)
    }

    /**
     * Show confirmation message for unlink bank account
     */
    private fun performUnlinkBankAccount(account: PlaidInstitutionResponse) {
        mIdlingResource?.setIdleState(false)
        val message = String.format(getString(R.string.alert_unlink_bank_account), account.institutionName, getString(R.string.application_name), getString(R.string.application_name))
        val title = String.format(getString(R.string.title_unlink_bank_account), account.institutionName)
        DialogUtil.alert(context = mBaseActivity!!, title = title, msg = message,
                positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Delete Bank Account")
                        showHideProgressDialog(true)
                        viewModel.callDeleteBankAccount(account.institutionId ?: "")
                        deletedAccount = account
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        mIdlingResource?.setIdleState(true)
                    }
                }, isCancelable = true)
    }


    private val myPlaidLinkActivityResultHandler by lazy {
        PlaidLinkResultHandler(
                requestCode = IConstants.REQUEST_PLAID_LINK_CODE,
                onSuccess = {
                    var accountIds = ""
                    var accountNames = ""
                    var accountNumbers = ""
                    it.linkConnectionMetadata.accounts.forEachIndexed { index, linkAccount ->
                        if(index==0){
                            accountIds = linkAccount.accountId
                            accountNames = linkAccount.accountName?:""
                            accountNumbers = linkAccount.accountNumber?:""
                        }else if(index<=2){
                            accountIds += ", " + linkAccount.accountId
                            accountNames += ", " +linkAccount.accountName?:""
                            accountNumbers += ", " +linkAccount.accountNumber?:""
                        }

                    }
                    var info = "##Public Token: "+it.publicToken+"                   ##Account Ids:"+accountIds+"         ##Account Names: "+accountNames+"                   ##Account Numbers: "+accountNumbers+"          ##Institution Id: "+it.linkConnectionMetadata.institutionId+"         ##Institution Name: "+it.linkConnectionMetadata.institutionName
                    logger.debugEvent("Plaid Success", info)
                    if (mBaseActivity!!.isNetworkConnected()) {
                        showHideProgressDialog(true)
                        viewModel.callAddBankAccount(it)
                    } else {
                        mIdlingResource?.setIdleState(true)
                    }
                },
                onCancelled = {
                    logger.debugEvent("Plaid Cancelled", getString(
                            R.string.content_cancelled,
                            it.institutionId ?: "",
                            it.institutionName ?: "",
                            it.linkSessionId ?: "",
                            it.status ?: ""
                    ), status = CustomLog.STATUS_ERROR)
                    it.institutionName ?: "" + " Failed after " +
//                    it.status?.showSnackBar(mBaseActivity)
                    mIdlingResource?.setIdleState(true)
                },
                onExit = {
                    logger.debugEvent("Plaid Exit", getString(
                            R.string.content_exit,
                            it.displayMessage ?: "",
                            it.errorCode ?: "",
                            it.errorMessage ?: "",
                            it.linkExitMetadata?.institutionId ?: "",
                            it.linkExitMetadata?.institutionName ?: "",
                            it.linkExitMetadata?.status ?: ""
                    ))
//                    "Plaid Exit with" + it.displayMessage?.showSnackBar(mBaseActivity)
                    mIdlingResource?.setIdleState(true)
                }
        )
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

    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }
        return mIdlingResource as RemoteIdlingResource
    }
}