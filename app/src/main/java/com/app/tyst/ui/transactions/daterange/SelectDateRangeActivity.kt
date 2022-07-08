package com.app.tyst.ui.transactions.daterange

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.tyst.R
import com.app.tyst.data.model.response.InstitutionsTotalAmountItem
import com.app.tyst.databinding.ActivitySelectDateRangeBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.transactions.account.AccountTransactionsListActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.extension.toServerDateFormatString
import java.util.*

/**
 * This activity is used for select date range
 */
class SelectDateRangeActivity : BaseActivity() {

    lateinit var binding: ActivitySelectDateRangeBinding
    private var generateLog = false

    companion object {
        /**
         * Start intent to open AccountTransactionsListActivity
         * @param mContext Context
         * @param isForGenerateLog
         * @param showReset
         * @return Intent
         */
        fun getStartIntent(mContext: Context, isForGenerateLog: Boolean = false, showReset: Boolean = false): Intent {
            return Intent(mContext, SelectDateRangeActivity::class.java).apply {
                putExtra("isForGenerateLog", isForGenerateLog)
                putExtra("showReset", showReset)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SelectDateRangeActivity, R.layout.activity_select_date_range)
        initView()
    }

    private fun initView() {
        setIntentData()
        initListener()
        val startMonth = Calendar.getInstance()
        startMonth.add(Calendar.MONTH, -(Calendar.getInstance().get(Calendar.MONTH) + 12))
        val endMonth = Calendar.getInstance()
        endMonth.add(Calendar.MONTH, 0)
        logger.debugEvent("Date Range", "Start month: " + startMonth.time.toString() + " :: End month: " + endMonth.time.toString())
        binding.calendarPicker.setVisibleMonthRange(startMonth, endMonth)
        binding.calendarPicker.setCurrentMonth(Calendar.getInstance())
    }

    /**
     * Set institutionId, start date and end date from selected account
     */
    private fun setIntentData() {
        binding.btnSelect.text = if (intent?.getBooleanExtra("isForGenerateLog", false) == true) {
            generateLog = true
            getString(R.string.generate_log)
        } else {
            generateLog = false
            getString(R.string.select)
        }

        if (intent?.getBooleanExtra("showReset", false) == true) {
            showInterstitial()
            binding.btnReset.visibility = View.VISIBLE
        } else {
            binding.btnReset.visibility = View.GONE
        }
    }

    private fun initListener() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce {
                finish()
            }

            btnSelect.makeBounceable()
            btnSelect.clickWithDebounce {
                sendResult(false)
            }

            btnReset.makeBounceable()
            btnReset.clickWithDebounce {
                sendResult(true)
            }
        }
    }

    /**
     * Send selected date range to calling activity
     */
    private fun sendResult(reset: Boolean) {
        // if you want to send any type of data back, such as an object, string, int etc
        // you have to create an intent and use this bit of code
        // otherwise you can just use setResult(Activity.RESULT_OK) and finish()
        try {
            val resultIntent = Intent()
            if (reset) {
                resultIntent.putExtra("isReset", reset)
            } else {
                val startDate = binding.calendarPicker.startDate.time.toServerDateFormatString()
                val endDate = binding.calendarPicker.endDate.time.toServerDateFormatString()
                resultIntent.putExtra("startDate", startDate)
                resultIntent.putExtra("endDate", endDate)
            }
            if (generateLog) {
                setFireBaseAnalyticsData("id-createstatement", "click_createstatement", "click_createstatement")
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } catch (ex: Exception) {
            getString(R.string.alert_select_start_end_date).showSnackBar(this@SelectDateRangeActivity)
            logger.dumpCrashEvent(ex)
        }
    }
}
