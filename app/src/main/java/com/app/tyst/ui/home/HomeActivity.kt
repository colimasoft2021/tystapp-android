package com.app.tyst.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.request.GetPlaidTransactionsRequest
import com.app.tyst.databinding.ActivityHomeBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.receipts.ReceiptsFragment
import com.app.tyst.ui.export.ExportFragment
import com.app.tyst.ui.myprofile.MyProfileFragment
import com.app.tyst.ui.settings.SettingsFragment
import com.app.tyst.ui.settings.inappbilling.BillingRepository
import com.app.tyst.ui.transactions.receipt.AddReceiptActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.extension.toEncrypt
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ncapdevi.fragnav.FragNavController
import com.github.dhaval2404.imagepicker.ImagePicker


class HomeActivity : BaseActivity(), FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    companion object {
        const val INDEX_HOME = FragNavController.TAB1
        const val INDEX_EXPORT = FragNavController.TAB2
        const val INDEX_RECEIPT = FragNavController.TAB3
        const val INDEX_MY_PROFILE = FragNavController.TAB4
        const val INDEX_SETTING = FragNavController.TAB5
        const val TAG = "MainActivity"
    }

    lateinit var binding: ActivityHomeBinding
    private var mFragNavController: FragNavController? = null
    private var lastSelectedTab = INDEX_HOME
    private var navigationAdapter: AHBottomNavigationAdapter? = null
    override val numberOfRootFragments: Int = 5
    var newAccountAdded = MutableLiveData<Boolean>()
    var accountDeleted = MutableLiveData<Boolean>()
    var newReceiptAdded = MutableLiveData<Boolean>()


    private val viewModel: HomeViewModel
        get() = ViewModelProviders.of(this@HomeActivity).get(HomeViewModel::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@HomeActivity, R.layout.activity_home)
        initView(savedInstanceState = savedInstanceState)
    }

    private fun initView(savedInstanceState: Bundle?) {
        setupBottomBar()
        setupFragments(savedInstanceState)
    }

    /**
     * Setup Bottom bar navigation
     */
    private fun setupBottomBar() {
        navigationAdapter = AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation)
        navigationAdapter?.setupWithBottomNavigation(binding.bottomNavigation)
        binding.bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        binding.bottomNavigation.isTranslucentNavigationEnabled = true
        binding.bottomNavigation.isBehaviorTranslationEnabled = false
        binding.bottomNavigation.setSelectedBackgroundVisible(false)
        // Set background color
        binding.bottomNavigation.inactiveColor = ContextCompat.getColor(this, R.color.colorGrey)

        binding.bottomNavigation.accentColor = ContextCompat.getColor(this, R.color.colorPrimary)
//        binding.bottomNavigation.setTitleTypeface(ResourcesCompat.getFont(this, R.font.nunito_bold))
        binding.bottomNavigation.setTitleTextSize(resources.getDimension(R.dimen._12sdp), resources.getDimension(R.dimen._12sdp))
        binding.bottomNavigation.setOnTabSelectedListener { position, _ ->
            mOnNavigationItemSelectedListener.onNavigationItemSelected(navigationAdapter?.getMenuItem(position)!!)
            true
        }
    }

    /**
     * Set Bottom Tab Fragments
     */
    private fun setupFragments(savedInstanceState: Bundle?) {

        mFragNavController = FragNavController(supportFragmentManager, R.id.frameContainer)
        mFragNavController?.fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
        mFragNavController?.rootFragmentListener = this@HomeActivity
        mFragNavController?.initialize(INDEX_HOME, savedInstanceState)
        mFragNavController?.switchTab(INDEX_HOME)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        mFragNavController?.onSaveInstanceState(outState!!)

    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

            R.id.action_home -> {
                switchTab(INDEX_HOME)
                return@OnNavigationItemSelectedListener true
            }


            R.id.action_export -> {
                if (sharedPreference.isLogin)
                    switchTab(INDEX_EXPORT)
                else
                    navigateToLoginScreen(false)
                return@OnNavigationItemSelectedListener true
            }

            R.id.action_receipts -> {
                if (sharedPreference.isLogin) {
                    //   switchTab(INDEX_RECEIPT)
                    startActivity(AddReceiptActivity.getStartIntent(this@HomeActivity, true))
                    Handler().postDelayed({ changeTab(lastSelectedTab) }, 2000)
                } else {
                    navigateToLoginScreen(false)
                }
                return@OnNavigationItemSelectedListener true
            }

            R.id.action_my_profile -> {
                if (sharedPreference.isLogin)
                    switchTab(INDEX_MY_PROFILE)
                else
                    navigateToLoginScreen(false)
                return@OnNavigationItemSelectedListener true
            }

            R.id.action_setting -> {
                switchTab(INDEX_SETTING)
                return@OnNavigationItemSelectedListener true
            }

        }
        false
    }

    private fun switchTab(pos: Int) {
        lastSelectedTab = pos
        if (mFragNavController?.currentStackIndex != pos) {
            mFragNavController?.switchTab(pos)
            if (!(mFragNavController?.currentStackIndex != pos || mFragNavController?.currentStack == null || mFragNavController?.currentStack?.size ?: 0 < 2)) {
                mFragNavController?.popFragments(mFragNavController?.currentStack?.size ?: 0 - 1)
            }
        }
        if (mFragNavController?.currentStackIndex == pos) {
            return
        }
    }

    fun changeTab(position: Int) {
        binding.bottomNavigation.postDelayed({ binding.bottomNavigation.currentItem = position }, 300)
        switchTab(position)
    }

    override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
    }

    override fun onTabTransaction(fragment: Fragment?, index: Int) {
    }

    override fun getRootFragment(index: Int): Fragment {
        when (index) {
            INDEX_HOME -> return HomeFragment()
            INDEX_RECEIPT -> return ReceiptsFragment()
            INDEX_EXPORT -> return ExportFragment()
            INDEX_MY_PROFILE -> return MyProfileFragment()
            INDEX_SETTING -> return SettingsFragment()
        }
        throw IllegalStateException("Need to send an index that we know")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IConstants.REQUEST_PLAID_LINK_CODE && mFragNavController?.currentFrag is MyProfileFragment) {
            (mFragNavController?.currentFrag as MyProfileFragment).onActivityResult(requestCode, resultCode, data)
        } else if (requestCode == IConstants.REQUEST_PLAID_LINK_CODE && mFragNavController?.currentFrag is HomeFragment) {
            (mFragNavController?.currentFrag as HomeFragment).onActivityResult(requestCode, resultCode, data)
        } else if (requestCode == ImagePicker.REQUEST_CODE && mFragNavController?.currentFrag is ReceiptsFragment) {
            (mFragNavController?.currentFrag as ReceiptsFragment).onActivityResult(requestCode, resultCode, data)
        }

    }

    fun getTransactions() {
        val request = GetPlaidTransactionsRequest(access_token = sharedPreference.getInstitutionsList()?.get(0)?.accessToken
                ?: "",
                client_id = "5da41bb5dd692400131870ad",
                secret = "f067b16bbf8ec5babcab185dd53943",
                start_date = "2019-01-01",
                end_date = "2019-02-01")
        viewModel.callGetPlaidTransactions(request)
    }

    private fun showWelcomeDialog() {
        DialogUtil.alert(context = this@HomeActivity, msg = getString(R.string.welcome_str),
                positiveBtnText = getString(R.string.ok), negativeBtnText = "",
                il = null, isCancelable = true)
    }

    fun addObserver() {
//        viewModel.transactionsData.observe(this@HomeActivity, Observer {
//            it.accounts
//        })
//
//        viewModel.plaidSettingDate.observe(this@HomeActivity, Observer {
//            if (it.settings?.isSuccess == true && it.data != null) {
//                sharedPreference.plaidSettings = it.data
//            }
//        })
    }
}
