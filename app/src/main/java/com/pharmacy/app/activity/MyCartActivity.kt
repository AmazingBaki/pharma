package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityMyCartBinding
import com.pharmacy.app.fragments.MyCartFragment
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.BroadcastReceiverExt
import com.pharmacy.app.utils.extensions.addFragment
import com.pharmacy.app.utils.extensions.getCartListFromPref
import com.pharmacy.app.utils.extensions.launchActivityWithNewTask

class MyCartActivity : BaseViewBindingActivity<ActivityMyCartBinding>() {

    override fun getViewBinding(): ActivityMyCartBinding {
        return ActivityMyCartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.menu_my_cart)

        val fr = MyCartFragment()
        addFragment(fr, R.id.container)
        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                if (fr.isAdded) {
                    fr.invalidateCartLayout(getCartListFromPref())
                }
            }
        }
    }

    fun shopNow() {
        launchActivityWithNewTask<DashBoardActivity>()
        finish()
    }

}
