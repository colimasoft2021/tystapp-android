package com.app.tyst.ui.settings.subscription

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PriceChangeFlowParams
import com.android.billingclient.api.SkuDetails
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.databinding.ActivitySubscriptionBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.settings.inappbilling.BillingRepository
import com.app.tyst.ui.settings.staticpages.StaticPagesActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.*
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG
import com.crashlytics.android.Crashlytics

class SubscriptionActivity : BaseActivity() {

    lateinit var binding: ActivitySubscriptionBinding
    private val viewModel: SubscriptionViewModel
        get() = ViewModelProviders.of(this).get(SubscriptionViewModel::class.java)
    private var monthlySubscriptionSKU: SkuDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SubscriptionActivity, R.layout.activity_subscription)
        initView()
    }

    private fun initView() {
        initListener()
        addObserver()
        setBoldAndColorSpannable(binding.tvTNC, getString(R.string.terms_n_conditions), getString(R.string.privacy_policy))
    }

    private fun initListener() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce {
                finish()
            }

            btnBuy.makeBounceable()
            btnBuy.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Purchase Subscription Click")
                setFireBaseAnalyticsData("id-buypremium", "click_buypremium", "click_buypremium")
                if (isNetworkConnected()) {
                    if (monthlySubscriptionSKU != null) {
                        viewModel.makePurchase(this@SubscriptionActivity, skuDetails = monthlySubscriptionSKU)
                    } else {
                        getString(R.string.msg_went_wrong).showSnackBar(this@SubscriptionActivity)
                    }
                }
            }

        }
    }

    private fun addObserver() {

        viewModel.monthlySubscriptionSKU.observe(this@SubscriptionActivity, Observer {
            monthlySubscriptionSKU = it
        })

        viewModel.purchaseData.observe(this@SubscriptionActivity, Observer {
            if (it != null) {
                val user = sharedPreference.userDetail
                user?.purchaseStatus = "Yes"
                sharedPreference.userDetail = user
                showHideProgressDialog(true)
                viewModel.callSubscriptionPurchase(it)
                (application as MainApplication).isAdRemoved.value = true
            }
        })

        viewModel.subsriptionPuchaseData.observe(this@SubscriptionActivity, Observer {
            showHideProgressDialog(false)
            finish()
            if (it.settings?.isSuccess == true) {

            }
        })
    }

    /**
     * Set spannable text for TNC and Privacy Policy
     * @param textView TextView
     * @param portions Array<out String>
     */
    private fun setBoldAndColorSpannable(textView: TextView, vararg portions: String) {
        val label = textView.text.toString()
        val spannableString1 = SpannableString(label)
        for (portion in portions) {
            val startIndex = label.indexOf(portion)
            val endIndex = startIndex + portion.length
            try {
                if (portion.equals(getString(R.string.terms_n_conditions), true))
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternetConnected(this@SubscriptionActivity)) {

                                startActivity(StaticPagesActivity.getStartIntent(this@SubscriptionActivity, IConstants.STATIC_PAGE_TERMS_CONDITION, getString(R.string.terms_n_conditions)))
                            } else {
                                getString(R.string.msg_check_internet_connection).showSnackBar(this@SubscriptionActivity, IConstants.SNAKBAR_TYPE_ERROR)

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = true // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                else if (portion.equals(getString(R.string.privacy_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternetConnected(this@SubscriptionActivity)) {

                                startActivity(StaticPagesActivity.getStartIntent(this@SubscriptionActivity, IConstants.STATIC_PAGE_PRIVACY_POLICY, getString(R.string.privacy_policy)))
                            } else {
                                getString(R.string.msg_check_internet_connection).showSnackBar(this@SubscriptionActivity, IConstants.SNAKBAR_TYPE_ERROR)

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = true // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                spannableString1.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@SubscriptionActivity, R.color.colorPrimary)), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.highlightColor = Color.TRANSPARENT
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        textView.text = spannableString1
    }

    private fun openPriceChangeDialog() {
        val priceChangeFlowParams = PriceChangeFlowParams.newBuilder()
                .setSkuDetails(monthlySubscriptionSKU)
                .build()

        viewModel.repository.getBillingClient()?.launchPriceChangeConfirmationFlow(this@SubscriptionActivity,
                priceChangeFlowParams
        ) { billingResult ->
            if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {

            } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

            }
        }
    }
}
