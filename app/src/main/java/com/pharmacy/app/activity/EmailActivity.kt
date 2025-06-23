package com.pharmacy.app.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityEmailBinding
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.*

class EmailActivity : BaseViewBindingActivity<ActivityEmailBinding>() {
    private lateinit var mMenuCart: View

    override fun getViewBinding(): ActivityEmailBinding {
        return ActivityEmailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.lbl_email)
        setToolbar(binding.toolbarInclude.toolbar)
        binding.btnSendMail.onClick {
            when {
                validate() -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.text_iqonicdesign_gmail_com), null))
                    emailIntent.putExtra(Intent.EXTRA_TEXT, binding.edtDescription.text.toString())
                    startActivity(Intent.createChooser(emailIntent, context.getString(R.string.lbl_send_email)))
                }
            }
        }

        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCount()
            }
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
        setCartCount()
        menuWishItem.actionView!!.onClick {
            launchActivity<MyCartActivity> { }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setCartCount() {
        val count = getCartCount()
        val notificationTextView = mMenuCart.findViewById<android.widget.TextView>(R.id.tvNotificationCount)
        notificationTextView.text = count
        if (count.checkIsEmpty()) {
            notificationTextView.hide()
        } else {
            notificationTextView.show()
        }
    }

    private fun validate(): Boolean {
        return when {
            binding.edtDescription.checkIsEmpty() -> {
                binding.edtDescription.showError(getString(R.string.error_field_required))
                false
            }
            else -> true
        }
    }

}
