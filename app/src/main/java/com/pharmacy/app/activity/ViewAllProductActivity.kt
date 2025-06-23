package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityViewAllBinding
import com.pharmacy.app.fragments.ViewAllProductFragment
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.BroadcastReceiverExt
import com.pharmacy.app.utils.extensions.replaceFragment

class ViewAllProductActivity : BaseViewBindingActivity<ActivityViewAllBinding>() {

    private var mFragment: ViewAllProductFragment? = null

    override fun getViewBinding(): ActivityViewAllBinding = ActivityViewAllBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)

        title = intent.getStringExtra(Constants.KeyIntent.TITLE)
        val mViewAllId = intent.getIntExtra(Constants.KeyIntent.VIEWALLID, 0)
        val mCategoryId = intent.getIntExtra(Constants.KeyIntent.KEYID, -1)

        mFragment = ViewAllProductFragment.getNewInstance(mViewAllId, mCategoryId)
        replaceFragment(mFragment!!, R.id.fragmentContainer)
        BroadcastReceiverExt(this) { onAction(CART_COUNT_CHANGE) { mFragment?.setCartCount() } }

    }
}