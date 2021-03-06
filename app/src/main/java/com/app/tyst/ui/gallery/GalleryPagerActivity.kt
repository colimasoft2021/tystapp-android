package com.app.tyst.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.app.tyst.R
import com.app.tyst.databinding.GalleryViewpagerBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.extension.clickWithDebounce

/**
 * This activity is used to show images in full screen with picnh zoom
 */
class GalleryPagerActivity : BaseActivity() {


    companion object {
        fun getStartIntent(mContext: Context, mediaList: ArrayList<String>, position: Int): Intent {
            val intent = Intent(mContext, GalleryPagerActivity::class.java)
            intent.putStringArrayListExtra(IConstants.BUNDLE_SELECTED_IMAGE, mediaList)
            intent.putExtra("position", position)
            return intent
        }
    }

    lateinit var binding: GalleryViewpagerBinding
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        binding = DataBindingUtil.setContentView(this@GalleryPagerActivity, R.layout.gallery_viewpager)

        val values = intent?.getStringArrayListExtra(IConstants.BUNDLE_SELECTED_IMAGE)
        val pos = intent.getIntExtra("position", 0)
        binding.vpGallery.adapter = GalleryPagerAdapter(values ?: ArrayList())
        binding.vpGallery.currentItem = pos
        binding.btnBack.clickWithDebounce { finish() }
    }
}