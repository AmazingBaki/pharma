package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.adapter.WalkAdapter
import com.pharmacy.app.databinding.ActivityWalkThroughBinding
import com.pharmacy.app.utils.CarouselEffectTransformer
import com.pharmacy.app.utils.Constants.SharedPref.SHOW_SWIPE
import com.pharmacy.app.utils.extensions.*

class WalkThroughActivity : BaseViewBindingActivity<ActivityWalkThroughBinding>() {
    private var mCount: Int? = null
    private var mHeading = arrayOf("Hi, We are Woobox!", "Most Unique Styles!", "Shop Till You Drop!")
    private val mSubHeading = arrayOf("We make around your city Affordable,\n easy and efficient.", "Shop the most trending fashion on the biggest shopping website.", "Grab the best seller pieces at bargain prices.")

    override fun getViewBinding(): ActivityWalkThroughBinding = ActivityWalkThroughBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        val adapter = WalkAdapter()

        binding.ViewPager.adapter = adapter

        binding.dots.attachViewPager(binding.ViewPager)
        binding.dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)
        mCount = adapter.count

        binding.btnStatShopping.onClick {
            getSharedPrefInstance().setValue(SHOW_SWIPE, true)
            launchActivityWithNewTask<DashBoardActivity>()
        }
        binding.llSignIn.onClick {
            launchActivity<SignInUpActivity>()
        }
    }

    private fun init() {
        binding.ViewPager.apply {
            clipChildren = false
            pageMargin = resources.getDimensionPixelOffset(R.dimen.spacing_small)
            offscreenPageLimit = 3
            setPageTransformer(false, CarouselEffectTransformer(this@WalkThroughActivity))
            offscreenPageLimit = 0

            onPageSelected { position: Int ->
                val animFadeIn = android.view.animation.AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
                binding.tvHeading.startAnimation(animFadeIn)
                binding.tvSubHeading.startAnimation(animFadeIn)
                binding.tvHeading.text = mHeading[position]
                binding.tvSubHeading.text = mSubHeading[position]
            }
        }
    }
}