package com.app.tyst.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.app.tyst.R
import com.app.tyst.databinding.ItemGalleryPagerBinding
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.bumptech.glide.Glide
import java.util.ArrayList

class GalleryPagerAdapter(private val mediaList: ArrayList<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, anyObject: Any): Boolean {
        return view == anyObject
    }

    override fun getCount(): Int {
        return mediaList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemGalleryPagerBinding.inflate(LayoutInflater.from(container.context))
        container.addView(binding.root)
        Glide.with(container.context)
                .load(mediaList[position])
                .error(R.drawable.user_profile)
                .placeholder(R.color.colorLightGray)
                .into(binding.ivGalleryImage)
        val imageMatrixTouchHandler = ImageMatrixTouchHandler(container.context)
        binding.ivGalleryImage.setOnTouchListener(imageMatrixTouchHandler)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }
}