package com.pharmacy.app.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityCardBinding
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.*

class CardActivity : BaseViewBindingActivity<ActivityCardBinding>() {

    private var mYearAdapter: ArrayAdapter<String>? = null
    private lateinit var mMenuCart: View
    var mMonthAdapter: ArrayAdapter<String>? = null

    override fun getViewBinding(): ActivityCardBinding {
        return ActivityCardBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.lbl_add_card)
        setToolbar(binding.toolbarInclude.toolbar)

        binding.edDigit1.addTextChangedListener(GenericTextWatcher(binding.edDigit1))
        binding.edDigit2.addTextChangedListener(GenericTextWatcher(binding.edDigit2))
        binding.edDigit3.addTextChangedListener(GenericTextWatcher(binding.edDigit3))
        binding.edDigit4.addTextChangedListener(GenericTextWatcher(binding.edDigit4))

        val mMonthList = ArrayList<String>()
        val mYearList = ArrayList<String>()
        mMonthList.add(0, "Select Month")
        mYearList.add(0, "Select Year")
        for (j in 1..12) {
            mMonthList.add(j.toString())
        }
        for (i in 2019..2040) {
            mYearList.add(i.toString())
        }

        mYearAdapter = ArrayAdapter(this, R.layout.spinner_items, mYearList)
        mMonthAdapter = ArrayAdapter(this, R.layout.spinner_items, mMonthList)

        binding.spYear.adapter = this.mYearAdapter
        binding.spMonth.adapter = mMonthAdapter

        binding.ivShowPwd.onClick {
            binding.ivShowPwd.hide()
            binding.ivHidePwd.show()
            binding.edCvv.setSelection(binding.edCvv.length())
            binding.edCvv.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        binding.ivHidePwd.onClick {
            binding.edCvv.setSelection(binding.edCvv.length())
            binding.edCvv.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivShowPwd.show()
            binding.ivHidePwd.hide()
        }
        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCount()
            }
        }
    }

    inner class GenericTextWatcher(private val view: View) : TextWatcher {

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (view.id) {
                R.id.edDigit1 -> if (text.length == 4)
                    binding.edDigit2.requestFocus()
                R.id.edDigit2 -> if (text.length == 4)
                    binding.edDigit3.requestFocus()
                else if (text.isEmpty())
                    binding.edDigit1.requestFocus()
                R.id.edDigit3 -> if (text.length == 4)
                    binding.edDigit4.requestFocus()
                else if (text.isEmpty())
                    binding.edDigit2.requestFocus()
                R.id.edDigit4 -> if (text.isEmpty())
                    binding.edDigit3.requestFocus()
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        val menuWishItem: MenuItem = menu!!.findItem(R.id.action_cart)
        val menuSearch: MenuItem = menu.findItem(R.id.action_search)
        menuWishItem.isVisible = true
        menuSearch.isVisible = false
        mMenuCart = menuWishItem.actionView!!
        mMenuCart.findViewById<android.widget.ImageView>(R.id.ivCart).setColorFilter(this.color(R.color.textColorPrimary))
        menuWishItem.actionView!!.onClick {
            launchActivity<MyCartActivity> { }
        }
        setCartCount()
        return super.onCreateOptionsMenu(menu)
    }

    fun setCartCount() {
        val count = getCartCount()
        mMenuCart.findViewById<android.widget.TextView>(R.id.tvNotificationCount).text = count
        if (count.checkIsEmpty()) {
            mMenuCart.findViewById<android.widget.TextView>(R.id.tvNotificationCount).hide()
        } else {
            mMenuCart.findViewById<android.widget.TextView>(R.id.tvNotificationCount).show()
        }
    }
}
