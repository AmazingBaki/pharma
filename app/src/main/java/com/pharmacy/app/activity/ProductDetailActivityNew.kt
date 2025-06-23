package com.pharmacy.app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.adapter.ProductImageAdapter
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.databinding.ActivityProductDetailNewBinding
import com.pharmacy.app.models.ProductModelNew
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants.KeyIntent.IS_ADDED_TO_CART
import com.pharmacy.app.utils.Constants.KeyIntent.PRODUCT_ID
import com.pharmacy.app.utils.extensions.*
import kotlin.math.abs


class ProductDetailActivityNew : BaseViewBindingActivity<ActivityProductDetailNewBinding>() {
    private var mPId = 0
    private val mImages = ArrayList<String>()
    private lateinit var mMenuCart: View
    private var isAddedTocart: Boolean = false
    var i: Int = 0
    private var mAttributeAdapter: RecyclerViewAdapter<String>? = null
    private var mYearAdapter: ArrayAdapter<String>? = null
    private var mProductModel: ProductModelNew? = null

    override fun getViewBinding(): ActivityProductDetailNewBinding = ActivityProductDetailNewBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar(binding.toolbar)

        if (intent?.extras?.get(PRODUCT_ID) == null) {
            toast(R.string.error_something_went_wrong)
            finish()
            return
        }
        getProductDetail()

        binding.tvItemProductOriginalPrice.applyStrike()

        isAddedTocart = intent.getBooleanExtra(IS_ADDED_TO_CART, false)
        if (!isAddedTocart) binding.btnAddCard.text =
                getString(R.string.lbl_add_to_cart) else binding.btnAddCard.text =
                getString(R.string.lbl_remove_cart)

        binding.btnAddCard.onClick {
            if (isLoggedIn()) {
                if (isAddedTocart) removeCartItem() else addItemToCart()
            } else launchActivity<SignInUpActivity>()

        }


        binding.toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar)
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (abs(verticalOffset) - binding.appBar.totalScrollRange == 0) {
                binding.toolbarLayout.title = binding.tvName.text
            } else {
                binding.toolbarLayout.title = ""
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        val menuWishItem: MenuItem = menu!!.findItem(R.id.action_cart)
        val menuSearch: MenuItem = menu.findItem(R.id.action_search)
        menuWishItem.isVisible = true
        menuSearch.isVisible = false
        mMenuCart = menuWishItem.actionView!!
        menuWishItem.actionView!!.onClick {
            launchActivity<MyCartActivity> { }
        }
        setCartCount()
        return super.onCreateOptionsMenu(menu)
    }


    fun setCartCount() {
        val count = getCartCount()
        mMenuCart.findViewById<TextView>(R.id.tvNotificationCount).text = count
        if (count.checkIsEmpty() || count == "0") {
            mMenuCart.findViewById<TextView>(R.id.tvNotificationCount).hide()
        } else {
            mMenuCart.findViewById<TextView>(R.id.tvNotificationCount).show()
        }
    }


    private fun addItemToCart() {
        if (mProductModel?.stock_quantity != null && mProductModel?.stock_quantity!! < 1) {
            toast(R.string.msg_sold_out); return
        }
        val requestModel = RequestModel(); requestModel.pro_id =
                mProductModel?.id.toString(); requestModel.quantity = 1;
        requestModel.color = "";requestModel.size = ""
        showProgress(true)
        callApi(getRestApis(false).addItemToCart(request = requestModel), onApiSuccess = {
            showProgress(false)
            snackBar(getString(R.string.success_add))
            fetchAndStoreCartData()
        }, onApiError = {
            showProgress(false)
            snackBarError(it)
            fetchAndStoreCartData()
        }, onNetworkError = {
            showProgress(false)
            noInternetSnackBar()
        })
        binding.btnAddCard.text = getString(R.string.lbl_remove_cart)
    }

    private fun removeCartItem() {
        getAlertDialog(getString(R.string.msg_confirmation), onPositiveClick = { dialog, i ->
            val requestModel = RequestModel()
            requestModel.pro_id = mProductModel?.id.toString()
            showProgress(true)
            callApi(getRestApis(false).removeCartItem(request = requestModel), onApiSuccess = {
                showProgress(false)
                binding.btnAddCard.text = getString(R.string.lbl_add_to_cart)
                snackBar(getString(R.string.success))
                fetchAndStoreCartData()
            }, onApiError = {
                showProgress(false)
                snackBarError(it)
                //getCartItems()
                fetchAndStoreCartData()
            }, onNetworkError = {
                showProgress(false)
                noInternetSnackBar()
            })
        }, onNegativeClick = { dialog, i ->
            dialog.dismiss()
        }).show()
    }

    private fun changeFavIcon(image: ImageView,
                              drawable: Int,
                              backgroundColor: Int,
                              iconTint: Int = R.color.textColorSecondary
    ) {
        image.setImageResource(drawable)
        image.applyColorFilter(color(iconTint))
    }

    private fun onFavouriteClick(image: ImageView) {
        if (isExistInWishList(mProductModel?.id!!)) {
            changeFavIcon(image, R.drawable.ic_heart, R.color.gray_80); binding.ivFavourite.isClickable = false
            val requestModel = RequestModel(); requestModel.pro_id =
                    mProductModel?.id.toString()
            removeFromWishList(requestModel) {
                binding.ivFavourite.isClickable = true
                if (it) changeFavIcon(image,
                        R.drawable.ic_heart,
                        R.color.gray_80
                ) else changeFavIcon(image,
                        R.drawable.ic_heart_fill,
                        R.color.favourite_background,
                        R.color.colorPrimary
                )
            }
        } else {
            if (isLoggedIn()) {
                changeFavIcon(image,
                        R.drawable.ic_heart_fill,
                        R.color.favourite_background,
                        R.color.colorPrimary
                ); binding.ivFavourite.isClickable = false

                val requestModel = RequestModel(); requestModel.pro_id =
                        mProductModel?.id.toString()
                addToWishList(requestModel) {
                    binding.ivFavourite.isClickable = true
                    if (it) changeFavIcon(image,
                            R.drawable.ic_heart_fill,
                            R.color.favourite_background,
                            R.color.colorPrimary
                    ) else changeFavIcon(image, R.drawable.ic_heart, R.color.gray_80)
                }
            } else {
                launchActivity<SignInUpActivity>()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getProductDetail() {
        mPId = intent?.getIntExtra(PRODUCT_ID, 0)!!
        binding.scrollView.visibility = View.GONE
        if (isNetworkAvailable()) {
            showProgress(true)
            callApi(getRestApis().listSingleProduct(mPId), onApiSuccess = {

                if (it.isEmpty()) {
                    showProgress(false)
                    finish()
                    return@callApi
                }
                mProductModel = it[0]

                binding.scrollView.visibility = View.VISIBLE
                /**
                 * Header Images
                 *
                 */
                binding.ivFavourite.onClick {
                    onFavouriteClick(this)
                }
                it[0].images.forEach { image ->
                    mImages.add(image.src)
                }
                binding.llReviews.onClick {
                    Log.e("pro id", mProductModel?.id.toString())
                    launchActivity<ReviewsActivity> {
                        putExtra(PRODUCT_ID, it[0].id)

                    }
                }
                binding.tvAllReviews.onClick {
                    Log.e("pro id", mProductModel?.id.toString())
                    launchActivity<ReviewsActivity> {
                        putExtra(PRODUCT_ID, it[0].id)

                    }
                }

                val adapter1 = ProductImageAdapter(mImages)
                binding.productViewPager.adapter = adapter1
                binding.dots.attachViewPager(binding.productViewPager)
                binding.dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)
                binding.tvItemProductOriginalPrice.applyStrike()
                /**
                 * Other Information
                 *
                 */
                binding.tvName.text = it[0].name
                binding.toolbarLayout.title = it[0].name
                binding.tvItemProductRating.rating = it[0].average_rating.toFloat()
                if (it[0].description.isNotEmpty()) {
                    binding.llDescription.show()

                    binding.tvTags.setText(it[0].description.getHtmlString().toString())
                    binding.tvTags.isMoreLessShow = true
                } else {
                    binding.llDescription.hide()
                }


                /**
                 * check stock
                 */
                if (it[0].in_stock) {
                    binding.btnOutOfStock.hide()
                    binding.btnAddCard.show()
                } else {
                    binding.btnOutOfStock.show()
                    binding.btnAddCard.hide()
                }

                /**
                 * Additional information
                 *
                 */
                if (it[0].attributes.isNotEmpty()) {
                    binding.llAdditionalInformation.show()

                    for (att in it[0].attributes!!) {
                        val vi =
                                applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val v: View = vi.inflate(R.layout.view_attributes, null)
                        val textView =
                                v.findViewById<View>(R.id.txtAttName) as TextView
                        textView.text = att.name.toString() + " : "

                        val sizeList = ArrayList<String>()
                        val sizes = att.options
                        sizes?.forEachIndexed { i, s ->
                            sizeList.add(s.trim())
                        }
                        mAttributeAdapter =
                                RecyclerViewAdapter(
                                        R.layout.item_attributes,
                                        onBind = { vv, item, position ->
                                            if (item.isNotEmpty()) {
                                                val attSize =
                                                        vv.findViewById<View>(R.id.tvSize) as TextView
                                                if (sizeList.size - 1 == position) {
                                                    attSize.text = item
                                                } else {
                                                    attSize.text = "$item ,"
                                                }
                                            }
                                        })
                        mAttributeAdapter?.addItems(sizeList)
                        val recycleView = v.findViewById<View>(R.id.rvAttributeView) as RecyclerView
                        recycleView.setHorizontalLayout()
                        recycleView.adapter = mAttributeAdapter

                        binding.llAttributeView.addView(
                                v,
                                0,
                                ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.FILL_PARENT,
                                        ViewGroup.LayoutParams.FILL_PARENT
                                )
                        )
                    }
                } else {
                    binding.llAdditionalInformation.hide()
                }

                /**
                 *  Attribute Information
                 */
                if (it[0].type == "simple") {
                    if (it[0].attributes.isNotEmpty()) {
                        binding.tvAvailability.text = it[0].attributes[0].name.toString()
                    }
                    binding.llAttribute.hide()
                    if (it[0].on_sale) {
                        binding.tvPrice.text = it[0].sale_price.currencyFormat()
                        binding.tvSale.show()
                        binding.tvItemProductOriginalPrice.applyStrike()
                        binding.tvItemProductOriginalPrice.text = it[0].regular_price.currencyFormat()
                        val discount =
                                calculateDiscount(it[0].sale_price, it[0].regular_price)
                        if (discount > 0.0) {
                            binding.tvSaleDiscount.visibility = View.VISIBLE
                            binding.tvSaleDiscount.text =
                                    String.format("%.2f", discount) + "% Off"
                        }
                        binding.upcomingSale.visibility = View.GONE
                    } else {
                        binding.tvPrice.text = it[0].price?.currencyFormat()
                        binding.tvSale.hide()
                        showUpComingSale(it[0])
                    }
                    mProductModel = it[0]
                } else if (it[0].type == "variable") {
                    binding.llAttribute.show()
                    mProductModel = it[0]
                    if (it[0].attributes.isNotEmpty()) {
                        val sizeList = ArrayList<String>()
                        val mVariationsList = ArrayList<Int>()
                        val mVariations = it[0].variations
                        it.forEachIndexed { i, details ->
                            if (i > 0) {
                                var option = ""
                                it[i].attributes!!.forEach { attr ->
                                    option = if (option.isNotBlank()) {
                                        option + " - " + attr.option.toString()
                                    } else {
                                        attr.option.toString()
                                    }
                                }
                                if (details.on_sale) {
                                    option = "$option [Sale]"
                                }
                                sizeList.add(option)
                            }
                        }

                        mVariations.forEachIndexed { index, s ->
                            mVariationsList.add(s)
                        }
                        mYearAdapter = ArrayAdapter(this, R.layout.spinner_items, sizeList)
                        binding.spAttribute.adapter = this.mYearAdapter

                        binding.spAttribute.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                            ) {
                                it.forEach { its ->
                                    if (mVariationsList[position] == its.id) {
                                        setPriceDetail(its)
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                            }
                        }

                    } else {
                        binding.llAttribute.hide()
                    }
                }

                // Purchasable
                if (!it[0].purchasable) {
                    binding.bannerContainer.hide()
                } else {
                    binding.bannerContainer.show()
                }

                // Review
                if (it[0].reviews_allowed) {
                    binding.tvAllReviews.show()
                    binding.llReviews.show()
                } else {
                    binding.llReviews.hide()
                    binding.tvAllReviews.hide()
                }
                showProgress(false)


            }, onApiError = {
                showProgress(false)
                snackBar(it)
            })
        }
    }

    private fun calculateDiscount(salePrice: String?, regularPrice: String?): Float {
        return (100f - (salePrice!!.toFloat() * 100f) / regularPrice!!.toFloat())
    }

    private fun getPriceDetails(details: ProductModelNew): String {
        val option = details.attributes!![0].option

        var price = ""
        price = if (details.on_sale) {
            val discount =
                    calculateDiscount(details.sale_price, details.regular_price)
            if (discount > 0.0) {
                details.sale_price.toString() + " [" + String.format("%.2f", discount) + "% Off]"
            } else {
                details.sale_price.toString()
            }
        } else {
            details.regular_price.toString()
        }
        return option + "-" + price.currencyFormat()
    }

    @SuppressLint("SetTextI18n")
    private fun setPriceDetail(its: ProductModelNew) {
        mProductModel = its
        if (its.on_sale) {
            binding.tvPrice.text = its.price.currencyFormat()
            binding.tvSale.show()
            binding.tvItemProductOriginalPrice.applyStrike()
            binding.tvItemProductOriginalPrice.text =
                    its.regular_price.currencyFormat()
            binding.upcomingSale.visibility = View.GONE
        } else {
            binding.tvItemProductOriginalPrice.text = ""
            binding.tvPrice.text = its.regular_price.currencyFormat()
            binding.tvSale.hide()
            showUpComingSale(its)
        }

        binding.tvAvailability.text = its.attributes[0].name.toString()

        mYearAdapter!!.notifyDataSetChanged()
    }

    /**
     * Show Upcoming sale details
     *
     */
    private fun showUpComingSale(its: ProductModelNew) {
        if (its.date_on_sale_from != "") {
            binding.upcomingSale.visibility = View.VISIBLE
            binding.tvUpcomingSale.text =
                    "Sale Start from " + its.date_on_sale_from + " to " + its.date_on_sale_to + ". Get amazing discounts on the products."
        } else {
            binding.upcomingSale.visibility = View.GONE
        }
    }
}

