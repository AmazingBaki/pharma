package com.pharmacy.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.pharmacy.app.R
import com.pharmacy.app.utils.extensions.loadImageFromUrl

class ProductImageAdapter(private var mImg: ArrayList<String>) : PagerAdapter() {

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_itemimage, parent, false)

        view.findViewById<ImageView>(R.id.imgSlider).loadImageFromUrl(mImg.get(position))

        parent.addView(view)
        return view
    }

    override fun isViewFromObject(v: View, `object`: Any): Boolean = v === `object` as View

    override fun getCount(): Int = mImg!!.size

    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) = parent.removeView(`object` as View)

}