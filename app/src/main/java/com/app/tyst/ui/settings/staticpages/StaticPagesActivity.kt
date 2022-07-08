package com.app.tyst.ui.settings.staticpages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.data.model.response.VersionConfigResponse
import com.app.tyst.databinding.ActivityStaticPagesBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.*

class StaticPagesActivity : BaseActivity() {

    companion object {

        /**
         * Start intent to open StaticPagesActivity
         * @param mContext [ERROR : Context]
         * @param pageCode String Code of static page, which need to show
         * @return Intent
         */
        fun getStartIntent(mContext: Context, pageCode: String, title:String, forceUpdate: Boolean = false, versionResponse: VersionConfigResponse = VersionConfigResponse()): Intent {
            return Intent(mContext, StaticPagesActivity::class.java).apply {
                putExtra("page_code", pageCode)
                putExtra("title", title)
                putExtra("force_update", forceUpdate)
                if (forceUpdate)
                    putExtra("version_check", versionResponse)
            }
        }
    }

    lateinit var binding: ActivityStaticPagesBinding
    private var pageCode = ""
    private var forceUpdate = false
    private var versionCheck: VersionConfigResponse? = null

    val viewModel: StaticPageViewModel
        get() = ViewModelProviders.of(this).get(StaticPageViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@StaticPagesActivity, R.layout.activity_static_pages)
        initView()
        initListeners()
        addObserver()
    }

    private fun initView() {
        pageCode = intent?.getStringExtra("page_code") ?: ""
//        binding.tvScreenName.text = intent?.getStringExtra("title") ?: ""
        forceUpdate = intent?.getBooleanExtra("force_update", false) ?: false
        if (forceUpdate)
            versionCheck = intent?.getParcelableExtra("version_check")
        binding.tvStaticData.movementMethod = LinkMovementMethod.getInstance()
        binding.tvStaticData.setLinkTextColor(ContextCompat.getColor(this@StaticPagesActivity, R.color.colorBlue))
        binding.tvPatent.visibility = if(pageCode == IConstants.STATIC_PAGE_ABOUT_US) View.VISIBLE else View.GONE

        binding.btnBack.visibility = if (forceUpdate) View.INVISIBLE else View.VISIBLE
        binding.btnAgree.visibility = if (forceUpdate) View.VISIBLE else View.GONE

        if (versionCheck?.shouldShowPrivacyPolicyUpdated() == true && versionCheck?.shouldShowTNCUpdated() == true) {
            pageCode = IConstants.STATIC_PAGE_PRIVACY_POLICY
        }
        Handler().postDelayed({
            /*Call Static Page Api*/
            showHideProgressDialog(true)
            viewModel.callStaticPage(pageCode)
        },500)

        binding.srl.setSwipeToRefreshColor()
        binding.srl.setOnRefreshListener {
            if (isNetworkConnected()) {
                viewModel.callStaticPage(pageCode)
            } else {
                binding.srl.isRefreshing = false
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }

            btnAgree.makeBounceable()
            btnAgree.clickWithDebounce {
                showHideProgressDialog(true)
                viewModel.callUpdateTNCPrivacyPolicy(pageCode)
            }
        }
    }

    /**Load data in web view after response**/
    private fun addObserver() {
        viewModel.staticPageiveData.observe(this, Observer {
            showHideProgressDialog(false)
            binding.srl.isRefreshing = false
            if (it.settings?.isSuccess == true) {
                binding.tvScreenName.text = it.data?.pageTitle
                binding.tvStaticData.text = it.data?.content?.getHtmlFormattedText()
            } else if (it.settings?.isSuccess == false) {
                it?.settings?.message?.showSnackBar(this@StaticPagesActivity)
            } else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@StaticPagesActivity)
            }
        })

        viewModel.settingObserver.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.isSuccess == true) {
                if (versionCheck?.shouldShowPrivacyPolicyUpdated() == true && versionCheck?.shouldShowTNCUpdated() == true) {
                    versionCheck?.privacyPolicyUpdated = "0"
                    showHideProgressDialog(true)
                    pageCode = IConstants.STATIC_PAGE_TERMS_CONDITION
                    viewModel.callStaticPage(pageCode)
                } else {
                    finish()
                }
            } else {
                it.message.showSnackBar(this@StaticPagesActivity)
            }
        })
    }

    override fun onBackPressed() {
        if(!forceUpdate)
        super.onBackPressed()
    }

}
