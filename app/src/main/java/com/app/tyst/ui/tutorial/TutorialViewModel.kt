package com.app.tyst.ui.tutorial

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.app.tyst.R
import com.app.tyst.ui.core.BaseViewModel

class TutorialViewModel(app: Application) : BaseViewModel(app) {
    var screenNumber: Int = 0
    var title: String = ""
    var desc: String = ""
    var tutorialImage:Drawable?=null

    fun setInfo(){
        when (screenNumber) {
//            1 -> {
//                title = app.getString(R.string.subscription)
//                desc = app.getString(R.string.desc_subscription)
//                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_1)
//            }
            1 -> {
                title = app.getString(R.string.view_your_tex)
                desc = app.getString(R.string.desc_view_your_tex)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_2)
            }
            2 -> {
                title = app.getString(R.string.link_bank_accounts)
                desc = app.getString(R.string.desc_link_bank_accounts)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_3)
            }
            3 -> {
                title = app.getString(R.string.differentiate_tax)
                desc = app.getString(R.string.tax_calculated)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_4)
            }
            4 -> {
                title = app.getString(R.string.add_receipts_manually)
                desc = app.getString(R.string.desc_add_receipts_manually)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_5)
            }
            5 -> {
                title = app.getString(R.string.get_your_transaction_details)
                desc = app.getString(R.string.desc_get_your_transaction_details)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_6)
            }
            6 -> {
                title = app.getString(R.string.change_estimated_sales_tax)
                desc = app.getString(R.string.desc_change_estimated_sales_tax)
                tutorialImage = ContextCompat.getDrawable(app.baseContext, R.drawable.screen_7)
            }
        }
    }
}