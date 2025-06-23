package com.pharmacy.app.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityContactUsBinding
import com.pharmacy.app.utils.extensions.*

class ContactUsActivity : BaseViewBindingActivity<ActivityContactUsBinding>() {
    private lateinit var mMenuCart: View

    override fun getViewBinding(): ActivityContactUsBinding {
        return ActivityContactUsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_contactus)
        setToolbar(binding.toolbarInclude.toolbar)

        binding.llCallRequest.onClick {
            dialNumber(getString(R.string.contact_phone))
        }
        binding.llEmail.onClick {
            launchActivity<EmailActivity>()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        val menuWishItem: MenuItem = menu!!.findItem(R.id.action_cart)
        val menuSearch: MenuItem = menu.findItem(R.id.action_search)
        menuWishItem.isVisible = true
        menuSearch.isVisible = false
        mMenuCart = menuWishItem.actionView!!
        mMenuCart.findViewById<android.widget.ImageView>(R.id.ivCart).setColorFilter(this.color(R.color.textColorPrimary))

        menuWishItem.actionView?.onClick { launchActivity<MyCartActivity> { } }
        setCartCount()
        return super.onCreateOptionsMenu(menu)
    }

    fun setCartCount() {
        val count = getCartCount()
        val notificationTextView = mMenuCart.findViewById<android.widget.TextView>(R.id.tvNotificationCount)
        notificationTextView.text = count
        if (count.checkIsEmpty()) {
            notificationTextView.hide()
        } else {
            notificationTextView.show()
        }
    }
}
