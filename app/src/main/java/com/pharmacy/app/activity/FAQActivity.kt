package com.pharmacy.app.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.base.IExpandableListAdapter
import com.pharmacy.app.databinding.ActivityFaqactrivityBinding
import com.pharmacy.app.databinding.ItemFaqHeadingBinding
import com.pharmacy.app.databinding.ItemFaqSubheadingBinding
import com.pharmacy.app.models.Category
import com.pharmacy.app.models.SubCategory
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.*

class FAQActivity : BaseViewBindingActivity<ActivityFaqactrivityBinding>() {
    private lateinit var mMenuCart: View
    private lateinit var mFaqAdapter: IExpandableListAdapter<Category, SubCategory, ItemFaqHeadingBinding, ItemFaqSubheadingBinding>

    override fun getViewBinding(): ActivityFaqactrivityBinding {
        return ActivityFaqactrivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_faq)
        setToolbar(binding.toolbarInclude.toolbar)
        setFaq()

        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCount()
            }
        }
    }

    private fun setFaq() {
        mFaqAdapter = object : IExpandableListAdapter<Category, SubCategory, ItemFaqHeadingBinding, ItemFaqSubheadingBinding>(this) {
            override fun bindChildView(view: ItemFaqSubheadingBinding, childObject: SubCategory, groupPosition: Int, childPosition: Int): ItemFaqSubheadingBinding {
                return view
            }

            override fun bindGroupView(view: ItemFaqHeadingBinding, groupObject: Category, groupPosition: Int): ItemFaqHeadingBinding {
                return view
            }

            override val childItemResId: Int = R.layout.item_faq_subheading

            override val groupItemResId: Int = R.layout.item_faq_heading
        }
        binding.exFaq.setAdapter(mFaqAdapter)
        addItems()
    }

    private fun addItems() {
        val mHeading = arrayOf(getString(R.string.lbl_account_deactivate), getString(R.string.lbl_quick_pay), getString(R.string.lbl_return_items), getString(R.string.lbl_replace_items))
        val mSubHeading = arrayOf(getString(R.string.lbl_account_deactivate), getString(R.string.lbl_quick_pay), getString(R.string.lbl_return_items), getString(R.string.lbl_replace_items))
        val map = HashMap<Category, ArrayList<SubCategory>>()
        val mFaqList = ArrayList<Category>()
        mHeading.forEachIndexed { i: Int, s: String ->
            val heading = Category()
            heading.category_name = s
            mFaqList.add(heading)
        }
        mFaqList.forEach {
            val list = ArrayList<SubCategory>()
            mSubHeading.forEach {
                val subCat = SubCategory()
                subCat.subcategory_name = it
                list.add(subCat)
            }
            map[it] = list
        }
        mFaqAdapter.addExpandableItems(mFaqList, map)
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
