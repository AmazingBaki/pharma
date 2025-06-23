package com.pharmacy.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.pharmacy.app.R
import com.pharmacy.app.models.SliderImagesResponse
import com.pharmacy.app.utils.extensions.loadImageFromUrl
import com.pharmacy.app.utils.extensions.onClick
import com.pharmacy.app.utils.extensions.openCustomTab

class HomeSliderAdapter(var context: Context, private var mImg: ArrayList<SliderImagesResponse>) : PagerAdapter() {
    var size: Int? = null

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)

        val imgSlider = view.findViewById<ImageView>(R.id.imgSlider)
        imgSlider.loadImageFromUrl(mImg[position].image)
        imgSlider.onClick { if (mImg[position].url.isNotEmpty()) context.openCustomTab(mImg[position].url) }

        parent.addView(view)
        return view
    }

    override fun isViewFromObject(v: View, `object`: Any): Boolean = v === `object` as View

    override fun getCount(): Int = mImg.size
    // override fun getCount(): Int = if (size == null || mImg.size <= size!!) mImg.size  else  size!!
    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) = parent.removeView(`object` as View)

}
