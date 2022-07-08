package com.app.tyst.ui.tutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.app.tyst.R
import com.app.tyst.databinding.ActivityTutorialBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.sharedPreference
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : BaseActivity() {

    lateinit var bindind: ActivityTutorialBinding
    private var isFromSetting = true

    companion object {

        fun getStartIntent(mContext: Context, isFromSetting: Boolean): Intent {
            return Intent(mContext, TutorialActivity::class.java).apply {
                putExtra("isFromSetting", isFromSetting)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = DataBindingUtil.setContentView(this@TutorialActivity, R.layout.activity_tutorial)
        initView()
    }

    fun initView() {
        isFromSetting = intent?.getBooleanExtra("isFromSetting", true) ?: true
        bindind.viewPager.adapter = TutorialViewPagerAdapter(supportFragmentManager)
        bindind.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position <= 0) {
                    bindind.btnPrevious.visibility = View.GONE
                } else {
                    bindind.btnPrevious.visibility = View.VISIBLE
                }

                if (position == 5) {
                    bindind.btnNext.text = getString(R.string.done)
                } else {
                    bindind.btnNext.text = getString(R.string.next)
                }
            }

        })
        initListeners()
    }

    fun initListeners() {
        if (isFromSetting) {
            bindind.btnBack.visibility = View.VISIBLE
            bindind.btnSkip.visibility = View.GONE
            bindind.btnBack.makeBounceable()
            bindind.btnBack.clickWithDebounce { finish() }
        } else {
            bindind.btnBack.visibility = View.GONE
            bindind.btnSkip.visibility = View.VISIBLE
            bindind.btnSkip.makeBounceable()
            bindind.btnSkip.clickWithDebounce {
                sharedPreference.isTutorialShowed = true
                navigateToHomeScreen()
            }
        }

        bindind.btnPrevious.makeBounceable()
        bindind.btnPrevious.clickWithDebounce {
            if (bindind.viewPager.currentItem > 0) {
                bindind.viewPager.setCurrentItem(viewPager.currentItem - 1, true)

            }
        }

        bindind.btnNext.makeBounceable()
        bindind.btnNext.clickWithDebounce {
            if (bindind.viewPager.currentItem < 5) {
                bindind.viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            } else {
                if (isFromSetting) {
                    finish()
                } else {
                    sharedPreference.isTutorialShowed = true
                    navigateToHomeScreen()
                }
            }
        }

    }
}
