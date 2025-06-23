package com.pharmacy.app.activity

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.databinding.ActivityOfferBinding
import com.pharmacy.app.models.ProductDataNew
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.extensions.*

class OfferActivity : BaseViewBindingActivity<ActivityOfferBinding>() {
    private lateinit var mMenuCart: View
    private val mImg = ArrayList<String>()

    private val mPage = 1
    private var mIsLoading = false
    private var countLoadMore = 1
    private var offerAdapter: RecyclerViewAdapter<ProductDataNew>? = null

    override fun getViewBinding(): ActivityOfferBinding {
        return ActivityOfferBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.title_offer)

        getOffers()
        BroadcastReceiverExt(this) { onAction(CART_COUNT_CHANGE) { setCartCount() } }
        loadOffers(mPage)
    }

    private fun getOffers() {
        getSlideImagesFromPref().forEach { mImg.add(it.image) }
        val handler = Handler()
        val runnable = object : Runnable {
            var i = 0

            override fun run() {
                i++
                if (i > mImg.size - 1) {
                    i = 0
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
        showProgress(false)

        offerAdapter = RecyclerViewAdapter(R.layout.item_offer) { view, item, position ->
            view.findViewById<android.widget.TextView>(R.id.tvProductName).text = item.name
            if (!(item.sale_price?.checkIsEmpty())!!) {
                view.findViewById<android.widget.TextView>(R.id.tvOffer).text = item.sale_price.currencyFormat()
            } else {
                view.findViewById<android.widget.TextView>(R.id.tvOffer).text = item.price?.currencyFormat()
            }

            if (item.full != null) view.findViewById<android.widget.ImageView>(R.id.ivProduct).loadImageFromUrl(item.full)
        }
        offerAdapter?.onItemClick = { pos, view, item -> showProductDetail(item) }

        val linearLayoutManager = GridLayoutManager(this, 2)

        binding.rcvOffer.apply {
            adapter = offerAdapter
            layoutManager = linearLayoutManager
            rvItemAnimation()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val countItem = linearLayoutManager.itemCount
                    val lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition()

                    if (!mIsLoading && countItem.minus(1) == lastVisiblePosition) {
                        mIsLoading = true

                        countLoadMore = countLoadMore.plus(1)
                        loadOffers(countLoadMore)
                    }
                }

            })
        }
        ViewCompat.setNestedScrollingEnabled(binding.rcvOffer, false)
    }

    private fun loadOffers(page: Int) {
        showProgress(true)
        val requestModel = RequestModel(); requestModel.page = page

        callApi(getRestApis(false).getOfferProducts(requestModel), onApiSuccess = {
            mIsLoading = it.size != 10; showProgress(false); offerAdapter?.addMoreItems(it)
        }, onApiError = {
            showProgress(false); snackBarError(it)
        }, onNetworkError = {
            showProgress(false); noInternetSnackBar()
        })
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
        menuWishItem.actionView!!.onClick { launchActivity<MyCartActivity>() }
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