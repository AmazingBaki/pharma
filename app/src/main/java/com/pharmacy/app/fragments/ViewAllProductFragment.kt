package com.pharmacy.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pharmacy.app.utils.rangeBar.RangeBar
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pharmacy.app.BaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.activity.MyCartActivity
import com.pharmacy.app.activity.SearchActivity
import com.pharmacy.app.activity.SignInUpActivity
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ItemNewestProductBinding
import com.pharmacy.app.databinding.ItemViewproductgridBinding
import com.pharmacy.app.models.FilterProductRequest
import com.pharmacy.app.models.ProductDataNew
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.models.RequestModel.*
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.ViewAllCode.CATEGORY_FEATURED
import com.pharmacy.app.utils.Constants.ViewAllCode.CATEGORY_NEWEST
import com.pharmacy.app.utils.Constants.ViewAllCode.FEATURED
import com.pharmacy.app.utils.Constants.ViewAllCode.NEWEST
import com.pharmacy.app.utils.Constants.ViewAllCode.RECENTSEARCH
import com.pharmacy.app.utils.HidingScrollListener
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentNewestProductBinding

class ViewAllProductFragment : BaseViewBindingFragment<FragmentNewestProductBinding>() {

    private var showPagination: Boolean? = true
    //region Variables
    private val mListAdapter = getAdapter()
    private val mGridAdapter = getGridAdapter()
    private var menuCart: View? = null
    private var mId: Int = 0
    private var mCategoryId: Int = -1

    private var mColor = ArrayList<FilterColors>()
    private var mBrand = ArrayList<FilterBrands>()
    private var mSize = ArrayList<FilterSizes>()
    private var mCategory = ArrayList<FilterCategories>()

    private var mSelectedColor: ArrayList<String> = ArrayList()
    private var mSelectedBrand: ArrayList<String> = ArrayList()
    private var mSelectedSize: ArrayList<String> = ArrayList()
    private var mSelectedCategory: ArrayList<Int> = ArrayList()
    private var mSelectedPrice: ArrayList<Int> = ArrayList()

    private var mIsFilterDataLoaded = false
    private lateinit var mProductAttributeResponseMsg: String
    var mFilterProductRequest = FilterProductRequest();
    private var mIsLoading = false
    private var countLoadMore = 1
    //endregion

    companion object {
        fun getNewInstance(
            id: Int,
            mCategoryId: Int,
            showPagination: Boolean = true
        ): ViewAllProductFragment {
            val fragment = ViewAllProductFragment()
            val bundle = Bundle()
            bundle.putSerializable(Constants.KeyIntent.VIEWALLID, id)
            bundle.putSerializable(Constants.KeyIntent.KEYID, mCategoryId)
            bundle.putSerializable(Constants.KeyIntent.SHOW_PAGINATION, showPagination)

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewestProductBinding {
        return FragmentNewestProductBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        mId = arguments?.getInt(Constants.KeyIntent.VIEWALLID)!!
        mCategoryId = arguments?.getInt(Constants.KeyIntent.KEYID)!!
        showPagination = arguments?.getBoolean(Constants.KeyIntent.SHOW_PAGINATION)
        mProductAttributeResponseMsg = getString(R.string.lbl_please_wait)
        setClickEventListener()
        binding.rvNewestProduct.apply {
            adapter = mGridAdapter
            rvItemAnimation()
            if (showPagination!!) {
                setOnScrollListener(object : HidingScrollListener(requireActivity()) {
                    override fun onMoved(distance: Int) {
                        binding.rlTop.translationY = -distance.toFloat()
                    }

                    override fun onShow() {
                        binding.rlTop.animate().translationY(0f).setInterpolator(DecelerateInterpolator(2f))
                            .start()
                    }

                    override fun onHide() {
                        binding.rlTop.animate().translationY((-binding.rlTop.height).toFloat())
                            .setInterpolator(AccelerateInterpolator(2f)).start()
                    }
                })
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        val countItem = recyclerView.layoutManager?.itemCount

                        var lastVisiblePosition = 0
                        if (recyclerView.layoutManager is LinearLayoutManager) {
                            lastVisiblePosition =
                                (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                        } else if (recyclerView.layoutManager is GridLayoutManager) {
                            lastVisiblePosition =
                                (recyclerView.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                        }

                        if (lastVisiblePosition != 0 && !mIsLoading && countItem?.minus(1) == lastVisiblePosition) {
                            mIsLoading = true
                            countLoadMore = countLoadMore.plus(1)
                            when (mId) {
                                CATEGORY_FEATURED -> {
                                    getSubCategoryProducts(countLoadMore = countLoadMore)
                                }
                                CATEGORY_NEWEST -> {
                                    getSubCategoryProducts(countLoadMore = countLoadMore)
                                }
                                FEATURED -> {
                                    getProducts(countLoadMore)
                                }
                                NEWEST -> {
                                   getProducts(countLoadMore)
                                }
                                else -> {
                                    getProducts(countLoadMore)
                                }
                            }
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                    }
                })

            } else {
                binding.rlTop.hide()
            }
        }

        when (mId) {
            RECENTSEARCH -> {
                mListAdapter.addItems(getRecentItems())
                mGridAdapter.addItems(getRecentItems())
            }
            CATEGORY_FEATURED -> {
                mSelectedCategory.add(mCategoryId)
                getSubCategoryProducts(countLoadMore = 1)
            }
            CATEGORY_NEWEST -> {
                mSelectedCategory.add(mCategoryId)
                getSubCategoryProducts(countLoadMore = 1)
            }
            FEATURED -> {
               getProducts()
            }
            NEWEST -> {
                getProducts()
            }
            else -> {
                getProducts()
            }
        }

        getProductAttributes()
        binding.ivGrid.performClick()
    }

    private fun getProductAttributes() {
        mProductAttributeResponseMsg = getString(R.string.lbl_please_wait)
        callApi(getRestApis(false).getProductAttributes(), onApiSuccess = {
            mIsFilterDataLoaded = true

            it.colors.forEachIndexed { index, color ->
                mColor.add(
                    FilterColors(
                        color.term_id,
                        color.name,
                        color.slug,
                        false
                    )
                )
            }
            it.brands.forEachIndexed { index, brand ->
                mBrand.add(
                    FilterBrands(
                        brand.term_id,
                        brand.name,
                        false
                    )
                )
            }
            it.sizes.forEachIndexed { index, size ->
                mSize.add(
                    FilterSizes(
                        size.term_id,
                        size.name,
                        false
                    )
                )
            }
            it.categories.forEachIndexed { index, category ->
                mCategory.add(
                    FilterCategories(
                        category.term_id,
                        category.cat_ID,
                        category.cat_name,
                        category.slug,
                        false
                    )
                )
            }
        }, onApiError = {
            activity?.snackBarError(it)
            mIsFilterDataLoaded = false
            mProductAttributeResponseMsg = getString(R.string.lbl_try_later)
        }, onNetworkError = {
            mIsFilterDataLoaded = false
            mProductAttributeResponseMsg = getString(R.string.lbl_try_later)
            activity?.noInternetSnackBar()
        })
    }

    private fun openFilterBottomSheet() {
        if (activity != null) {
            val filterDialog =
                BottomSheetDialog(requireActivity()); filterDialog.setContentView(R.layout.layout_filter)

            val priceArray = arrayOf(
                "0".currencyFormat(),
                "100".currencyFormat(),
                "200".currencyFormat(),
                "300".currencyFormat(),
                "400".currencyFormat(),
                "500".currencyFormat(),
                "600".currencyFormat(),
                "700".currencyFormat(),
                "800".currencyFormat(),
                "900".currencyFormat(),
                "1000".currencyFormat()
            )
            val priceArray2 =
                arrayOf("0", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000")

            val rangebar1 = filterDialog.findViewById<RangeBar>(R.id.rangebar1)
            rangebar1?.tickTopLabels = priceArray

            if (mSelectedPrice.size == 2) {
                rangebar1?.setRangePinsByValue(
                    priceArray2.indexOf(mSelectedPrice[0].toString()).toFloat(),
                    priceArray2.indexOf(mSelectedPrice[1].toString()).toFloat()
                )
            }

            //region Adapters


            val brandAdapter = RecyclerViewAdapter<FilterBrands>(
                R.layout.item_filter_brand,
                onBind = { view, item, position ->
                    val tvBrandName = view.findViewById<android.widget.TextView>(R.id.tvBrandName)
                    val ivSelect = view.findViewById<ImageView>(R.id.ivSelect)
                    
                    tvBrandName.text = item.name
                    if (item.isSelected != null && item.isSelected!!) {
                        tvBrandName.setTextColor(requireActivity().color(R.color.colorPrimary))
                        ivSelect.setImageResource(R.drawable.ic_check)
                        ivSelect.setColorFilter(requireActivity().color(R.color.colorAccent))
                        ivSelect.setStrokedBackground(
                            requireActivity().color(R.color.colorAccent),
                            requireActivity().color(R.color.colorAccent),
                            0.4f
                        )
                    } else {
                        tvBrandName.setTextColor(requireActivity().color(R.color.textColorSecondary))
                        ivSelect.setImageResource(0)
                        ivSelect.setStrokedBackground(requireActivity().color(R.color.checkbox_color))
                    }
                })
            brandAdapter.onItemClick = { pos, _, item ->
                item.isSelected = !(item.isSelected!!)
                brandAdapter.notifyItemChanged(pos)
            }
            val sizeAdapter = RecyclerViewAdapter<FilterSizes>(
                R.layout.item_size,
                onBind = { view, item, position ->
                    val ivSizeChecked = view.findViewById<android.widget.TextView>(R.id.ivSizeChecked)
                    
                    ivSizeChecked.text = item.name
                    ivSizeChecked.apply {
                        when {
                            item.isSelected!! -> {
                                setTextColor(requireActivity().color(R.color.commonColorWhite))
                                setStrokedBackground(requireActivity().color(R.color.colorPrimary))
                            }
                            else -> {
                                setTextColor(requireActivity().color(R.color.textColorPrimary))
                                setStrokedBackground(
                                    0,
                                    strokeColor = (requireActivity().color(R.color.view_color))
                                )
                            }
                        }

                    }

                })
            sizeAdapter.onItemClick = { pos, _, item ->
                item.isSelected = !(item.isSelected!!)
                sizeAdapter.notifyItemChanged(pos)
            }

            val colorAdapter = RecyclerViewAdapter<FilterColors>(
                R.layout.item_color,
                onBind = { view, item, position ->
                    val viewColor = view.findViewById<View>(R.id.viewColor)
                    val tvColor = view.findViewById<TextView>(R.id.tvColor)
                    val ivColorChecked = view.findViewById<ImageView>(R.id.ivColorChecked)

                    try {
                        if (item.name!!.contains("#")) {
                            viewColor.show()
                            tvColor.hide()
                            viewColor.setStrokedBackground(
                                Color.parseColor(item.name!!),
                                strokeColor = requireActivity().color(R.color.white)
                            )
                            ivColorChecked.setStrokedBackground(
                                Color.parseColor(item.name!!),
                                strokeColor = requireActivity().color(R.color.white)
                            )
                            when {
                                item.isSelected!! -> {
                                    viewColor.hide()
                                    ivColorChecked.show()
                                }
                                else -> {
                                    viewColor.show()
                                    ivColorChecked.hide()
                                }
                            }
                        } else {
                            viewColor.hide()
                            tvColor.show()
                            tvColor.text = item.name
                            tvColor.apply {
                                when {
                                    item.isSelected!! -> {
                                        setTextColor(requireActivity().color(R.color.commonColorWhite))
                                        setStrokedBackground(requireActivity().color(R.color.colorPrimary))
                                    }
                                    else -> {
                                        setTextColor(requireActivity().color(R.color.textColorPrimary))
                                        setStrokedBackground(
                                            0,
                                            strokeColor = requireActivity().color(R.color.view_color)
                                        )
                                    }
                                }

                            }

                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
            colorAdapter.onItemClick = { pos, _, item ->
                item.isSelected = !(item.isSelected!!)
                colorAdapter.notifyItemChanged(pos)
            }
            //endregion

            //region RecyclerViews
            if (mId != CATEGORY_FEATURED && mId != CATEGORY_NEWEST) {
                val categoryAdapter = RecyclerViewAdapter<FilterCategories>(
                    R.layout.item_filter_category,
                    onBind = { view, item, position ->
                        val tvSubCategory = view.findViewById<TextView>(R.id.tvSubCategory)
                        
                        tvSubCategory.text = item.cat_name?.getHtmlString()

                        if (item.isSelected != null && item.isSelected!!) {
                            tvSubCategory.setTextColor(requireActivity().color(R.color.white))
                            tvSubCategory.setStrokedBackground(requireActivity().color(R.color.colorPrimary))
                        } else {
                            tvSubCategory.setTextColor(requireActivity().color(R.color.colorPrimary))
                            tvSubCategory.setStrokedBackground(
                                requireActivity().color(R.color.white),
                                requireActivity().color(R.color.colorPrimary)
                            )
                        }
                    })
                categoryAdapter.onItemClick = { pos, _, item ->
                    item.isSelected = !(item.isSelected!!)
                    categoryAdapter.notifyItemChanged(pos)
                }
                val rcvSubCategories = filterDialog.findViewById<RecyclerView>(R.id.rcvSubCategories)
                rcvSubCategories?.apply {
                    layoutManager = FlexboxLayoutManager(requireActivity()).apply {
                        flexDirection = FlexDirection.ROW
                        flexWrap = FlexWrap.WRAP
                    }
                    itemAnimator = null; adapter = categoryAdapter
                }
                categoryAdapter.addItems(mCategory); categoryAdapter.setModelSize(5)
                val tvShowMore = filterDialog.findViewById<TextView>(R.id.tvShowMore)
                tvShowMore?.onClick {
                    if (categoryAdapter.size == 5) {
                        /*expand*/ categoryAdapter.setModelSize(mCategory.size); tvShowMore.text =
                            context.getString(R.string.lbl_less)
                    } else {
                        /*collapse*/ categoryAdapter.setModelSize(5); tvShowMore.text =
                            context.getString(R.string.lbl_more)
                    }
                }
            } else {
                val tvCategory = filterDialog.findViewById<TextView>(R.id.tv_category)
                val llCategory = filterDialog.findViewById<android.widget.LinearLayout>(R.id.ll_category)
                tvCategory?.hide()
                llCategory?.hide()
            }

            val rcvBrands = filterDialog.findViewById<RecyclerView>(R.id.rcvBrands)
            rcvBrands?.apply {
                setVerticalLayout(); adapter = brandAdapter
            }; brandAdapter.addItems(mBrand)
            val rcvSize = filterDialog.findViewById<RecyclerView>(R.id.rcvSize)
            rcvSize?.apply {
                setHorizontalLayout(); adapter = sizeAdapter
            }; sizeAdapter.addItems(mSize)
            val rcvColors = filterDialog.findViewById<RecyclerView>(R.id.rcvColors)
            rcvColors?.apply {
                setHorizontalLayout(); adapter = colorAdapter
            }; colorAdapter.addItems(mColor)
            //endregion

            //region Clicks
            val tvApply = filterDialog.findViewById<TextView>(R.id.tvApply)
            tvApply?.onClick {
                mSelectedBrand.clear(); mSelectedSize.clear(); mSelectedColor.clear(); mSelectedPrice.clear()
                if (mId != CATEGORY_FEATURED && mId != CATEGORY_NEWEST)mSelectedCategory.clear();
                mBrand.forEach { filterBrands ->
                    if (filterBrands.isSelected!!) mSelectedBrand.add(
                        filterBrands.name!!
                    )
                }
                mCategory.forEach { filterCategories ->
                    if (filterCategories.isSelected!!) mSelectedCategory.add(
                        filterCategories.cat_ID!!
                    )
                }
                mSize.forEach { filterSizes ->
                    if (filterSizes.isSelected!!) mSelectedSize.add(
                        filterSizes.name!!
                    )
                }
                mColor.forEach { filterColors ->
                    if (filterColors.isSelected!!) mSelectedColor.add(
                        filterColors.slug!!
                    )
                }

                rangebar1?.let {
                    mSelectedPrice.add(priceArray2[it.leftPinValue.toInt()].toInt())
                    mSelectedPrice.add(priceArray2[it.rightPinValue.toInt()].toInt())
                }

                if (mSelectedColor.isNotEmpty()) mFilterProductRequest.color = mSelectedColor else mFilterProductRequest.color=null
                if (mSelectedBrand.isNotEmpty()) mFilterProductRequest.brand = mSelectedBrand else mFilterProductRequest.brand = null
                if (mSelectedSize.isNotEmpty()) mFilterProductRequest.size = mSelectedSize else mFilterProductRequest.size = null
                if (mSelectedPrice.isNotEmpty()) mFilterProductRequest.price = mSelectedPrice else mFilterProductRequest.price = null
                if (mSelectedCategory.isNotEmpty()) mFilterProductRequest.category = mSelectedCategory else mFilterProductRequest.category = null
                countLoadMore=1
                mFilterProductRequest.page = countLoadMore
                filterProduct()
                filterDialog.dismiss()
            }
            val tvReset = filterDialog.findViewById<TextView>(R.id.tvReset)
            tvReset?.onClick {
                mColor.forEach { it.isSelected = false }
                mBrand.forEach { it.isSelected = false }
                mSize.forEach { it.isSelected = false }
                mCategory.forEach { it.isSelected = false }
                mSelectedPrice.clear()
                mFilterProductRequest.color = null
                mFilterProductRequest.brand = null
                mFilterProductRequest.size = null
                mFilterProductRequest.price = null
                if (mId == CATEGORY_NEWEST || mId == CATEGORY_FEATURED) {
                    mFilterProductRequest.category = mSelectedCategory
                }else{
                    mFilterProductRequest.category=null
                }
                countLoadMore=1
                mFilterProductRequest.page = countLoadMore
                filterProduct()
                filterDialog.dismiss()
            }
            val tvSelectAll = filterDialog.findViewById<TextView>(R.id.tvSelectAll)
            tvSelectAll?.onClick {
                mBrand.forEach { it.isSelected = true }
                brandAdapter.notifyDataSetChanged()
            }
            val ivClose = filterDialog.findViewById<ImageView>(R.id.ivClose)
            ivClose?.onClick { filterDialog.dismiss() }
            //endregion

            filterDialog.show()
        }
    }

    private fun filterProduct() {
        binding.progressBar.show(); binding.noDataInclude.rlNoData.hide(); binding.progressBar.animate()

        if (mId == FEATURED || mId == CATEGORY_FEATURED) {
            callApi(getRestApis(false).getFeaturedProducts(mFilterProductRequest), onApiSuccess = {
                if (activity == null) return@callApi
                mIsLoading = it.size != 10
                if (mListAdapter.itemCount == 0 && mGridAdapter.itemCount == 0) {
                    binding.ivGrid.performClick()
                }
                setProducts(it)
            }, onApiError = {
                if (activity == null) return@callApi
                binding.progressBar.hide()

                if (countLoadMore==1) {
                    noProductAvailable(it)
                }
            }, onNetworkError = {
                if (activity == null) return@callApi
                binding.progressBar.hide()
                activity?.noInternetSnackBar()
            })
        } else {
            callApi(getRestApis().filterProduct(request = mFilterProductRequest), onApiSuccess = {
                if (activity == null) return@callApi
                mIsLoading = it.size != 10
                if (mListAdapter.itemCount == 0 && mGridAdapter.itemCount == 0) {
                    binding.ivGrid.performClick()
                }
                setProducts(it)
            }, onApiError = {
                if (activity == null) return@callApi
                binding.progressBar.hide()

                if (countLoadMore==1) {
                    noProductAvailable(it)
                }
            }, onNetworkError = {
                if (activity == null) return@callApi
                binding.progressBar.hide()
                activity?.noInternetSnackBar()
            })
        }
    }

    private fun setProducts(it: ArrayList<ProductDataNew>) {
        binding.progressBar.hide()
        binding.progressBar.animate()
        if (countLoadMore == 1) {
            mListAdapter.clearData()
            mGridAdapter.clearData()
        }
        mListAdapter.addMoreItems(it)
        mGridAdapter.addMoreItems(it)

        if (mListAdapter.getModel().isEmpty() && mGridAdapter.getModel().isEmpty()) {
            binding.noDataInclude.rlNoData.show(); binding.rvNewestProduct.hide()
        } else {
            binding.noDataInclude.rlNoData.hide(); binding.rvNewestProduct.show()
        }
    }

    private fun noProductAvailable(it: String) {
        binding.progressBar.hide()
        if (it == "Sorry! No Product Available") {
            binding.noDataInclude.rlNoData.show()
            binding.rvNewestProduct.hide()
            mListAdapter.clearData()
            mGridAdapter.clearData()
            when (mId) {
                FEATURED -> {
                    mListAdapter.notifyDataSetChanged()
                    mGridAdapter.notifyDataSetChanged()
                }
                NEWEST -> {
                    mListAdapter.notifyDataSetChanged()
                    mGridAdapter.notifyDataSetChanged()
                }
            }
        } else {
            activity?.snackBarError(it)
        }
    }

    fun setCartCount() {
        val count = getCartCount(); menuCart?.findViewById<android.widget.TextView>(R.id.tvNotificationCount)?.text = count
        if (count.checkIsEmpty()) menuCart?.findViewById<android.widget.TextView>(R.id.tvNotificationCount)?.hide() else menuCart?.findViewById<android.widget.TextView>(R.id.tvNotificationCount)?.show()
    }

    private fun getSubCategoryProducts(countLoadMore: Int = 1) {
        mFilterProductRequest.page = countLoadMore
        if (mSelectedCategory.isNotEmpty()) mFilterProductRequest.category = mSelectedCategory

        filterProduct()
    }
    private fun getProducts(countLoadMore: Int = 1) {
        mFilterProductRequest.page = countLoadMore
        filterProduct()
    }
    private fun getAdapter(): BaseRecyclerAdapter<ProductDataNew, ItemNewestProductBinding> {

        return object : BaseRecyclerAdapter<ProductDataNew, ItemNewestProductBinding>() {

            override fun onItemClick(
                view: View,
                model: ProductDataNew,
                position: Int,
                dataBinding: ItemNewestProductBinding
            ) {
                when (view.id) {
                    R.id.ivDislike -> {
                        if (isLoggedIn()) {
                            dataBinding.ivDislike.hide()
                            dataBinding.ivlike.show()

                            val requestModel = RequestModel(); requestModel.pro_id =
                                model.pro_id.toString()
                            activity?.addToWishList(requestModel) {
                                if (activity == null) return@addToWishList
                                if (it) {
                                    dataBinding.ivDislike.hide(); dataBinding.ivlike.show()
                                } else {
                                    dataBinding.ivDislike.show(); dataBinding.ivlike.hide()
                                }
                            }
                        } else {
                            activity?.launchActivity<SignInUpActivity>()
                        }
                    }
                    R.id.ivlike -> {
                        dataBinding.ivDislike.show()
                        dataBinding.ivlike.hide()

                        val requestModel = RequestModel(); requestModel.pro_id =
                            model.pro_id.toString()
                        activity?.removeFromWishList(requestModel) {
                            if (activity == null) return@removeFromWishList
                            if (it) {
                                dataBinding.ivDislike.show(); dataBinding.ivlike.hide()
                            } else {
                                dataBinding.ivDislike.hide(); dataBinding.ivlike.show()
                            }
                        }
                    }
                    R.id.listProductRaw -> {
                        (activity as BaseActivity).showProductDetail(model)
                    }
                }
            }

            override val layoutResId: Int = R.layout.item_newest_product

            override fun onBindData(
                model: ProductDataNew,
                position: Int,
                dataBinding: ItemNewestProductBinding
            ) {
                if (model.full != null) dataBinding.ivProduct.loadImageFromUrl(model.full) else dataBinding.ivProduct.setImageResource(
                    0
                )
                if (model.sale_price!!.isNotEmpty()) {
                    dataBinding.tvProductPrice.text = model.sale_price.currencyFormat()
                    dataBinding.tvProductActualPrice.text = model.regular_price?.currencyFormat()
                    dataBinding.tvProductActualPrice.applyStrike()
                } else {
                    dataBinding.tvProductActualPrice.text = ""
                    dataBinding.tvProductPrice.text = model.price.toString().currencyFormat()
                }

                if (!isExistInWishList(model.pro_id!!)) {
                    dataBinding.ivDislike.show()
                    dataBinding.ivlike.hide()
                } else {
                    dataBinding.ivDislike.hide()
                    dataBinding.ivlike.show()
                }
            }

            override fun onItemLongClick(view: View, model: ProductDataNew, position: Int) {}
        }
    }

    private fun getGridAdapter(): BaseRecyclerAdapter<ProductDataNew, ItemViewproductgridBinding> {

        return object : BaseRecyclerAdapter<ProductDataNew, ItemViewproductgridBinding>() {

            override val layoutResId: Int = R.layout.item_viewproductgrid

            override fun onBindData(
                model: ProductDataNew,
                position: Int,
                dataBinding: ItemViewproductgridBinding
            ) {

                if (model.sale_price!!.isNotEmpty()) {
                    dataBinding.tvDiscountPrice.text = model.sale_price.currencyFormat()
                    dataBinding.tvOriginalPrice.text = model.regular_price?.currencyFormat()
                    dataBinding.tvOriginalPrice.applyStrike()
                }else{
                    dataBinding.tvOriginalPrice.text = ""
                    dataBinding.tvDiscountPrice.text = model.price.toString().currencyFormat()}


                dataBinding.ratingBar.rating = model.average_rating!!.toFloat()
                if (model.full != null) dataBinding.ivProduct.loadImageFromUrl(model.full) else dataBinding.ivProduct.setImageResource(
                    0
                )

                if (!isExistInWishList(model.pro_id!!)) {
                    dislikeProductImage(dataBinding.ivFavourite)
                } else {
                    likeProductImage(dataBinding.ivFavourite)
                }
            }

            override fun onItemClick(
                view: View,
                model: ProductDataNew,
                position: Int,
                dataBinding: ItemViewproductgridBinding
            ) {
                when (view.id) {
                    R.id.ivFavourite -> {
                        if (isExistInWishList(model.pro_id!!)) {
                            dislikeProductImage(dataBinding.ivFavourite)

                            val requestModel = RequestModel(); requestModel.pro_id =
                                model.pro_id.toString()
                            activity?.removeFromWishList(requestModel) {
                                if (activity == null) return@removeFromWishList
                                if (it) {
                                    hideProgress()
                                    dislikeProductImage(dataBinding.ivFavourite)
                                } else {
                                    hideProgress()
                                    likeProductImage(dataBinding.ivFavourite)
                                }
                            }
                        } else {
                            if (isLoggedIn()) {
                                likeProductImage(dataBinding.ivFavourite)

                                val requestModel = RequestModel()
                                requestModel.pro_id = model.pro_id.toString()
                                activity?.addToWishList(requestModel) {
                                    if (activity == null) return@addToWishList
                                    if (it) {
                                        hideProgress()
                                        likeProductImage(dataBinding.ivFavourite)
                                    } else {
                                        hideProgress()
                                        dislikeProductImage(dataBinding.ivFavourite)
                                    }
                                }
                            } else {
                                activity?.launchActivity<SignInUpActivity>()
                            }
                        }
                    }
                    R.id.gridProduct -> (activity as BaseActivity).showProductDetail(model)
                }
            }

            override fun onItemLongClick(view: View, model: ProductDataNew, position: Int) {}
        }
    }

    private fun dislikeProductImage(ivFavourite: ImageView) {
        ivFavourite.setImageResource(R.drawable.ic_heart)
        ivFavourite.applyColorFilter(requireActivity().color(R.color.textColorSecondary))
        ivFavourite.setStrokedBackground(requireActivity().color(R.color.gray_80))
    }

    private fun likeProductImage(ivFavourite: ImageView) {
        ivFavourite.setImageResource(R.drawable.ic_heart_fill)
        ivFavourite.applyColorFilter(requireActivity().color(R.color.colorPrimary))
        ivFavourite.setStrokedBackground(requireActivity().color(R.color.favourite_background))
    }

    private fun setClickEventListener() {
        binding.ivGrid.onClick {
            setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
            binding.ivList.setColorFilter(requireActivity().color(R.color.textColorSecondary))
            binding.rvNewestProduct.apply {
                layoutManager = GridLayoutManager(requireActivity(), 2)
                setHasFixedSize(true)
                binding.rvNewestProduct.adapter = mGridAdapter
                binding.rvNewestProduct.rvItemAnimation()
            }

        }
        binding.ivList.onClick {
            setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
            binding.ivGrid.setColorFilter(ContextCompat.getColor(context, R.color.textColorSecondary))
            binding.rvNewestProduct.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                setHasFixedSize(true)
                binding.rvNewestProduct.adapter = mListAdapter
                binding.rvNewestProduct.rvItemAnimation()
            }
        }
        binding.ivSort.onClick {
            val sortDialog =
                BottomSheetDialog(requireActivity()); sortDialog.setContentView(R.layout.dialog_sort)
            val txtHighPrice = sortDialog.findViewById<android.widget.TextView>(R.id.txtHighPrice)
            val txtLowPrice = sortDialog.findViewById<android.widget.TextView>(R.id.txtLowPrice)
            txtHighPrice?.onClick {
                mListAdapter.getModel().sortByDescending { it.price }
                mListAdapter.notifyDataSetChanged()
                mGridAdapter.getModel().sortByDescending { it.price }
                mGridAdapter.notifyDataSetChanged()
                sortDialog.dismiss()
            }
            txtLowPrice?.onClick {
                mListAdapter.getModel().sortBy { it.price }
                mListAdapter.notifyDataSetChanged()
                mGridAdapter.getModel().sortBy { it.price }
                mGridAdapter.notifyDataSetChanged()
                sortDialog.dismiss()
            }
            sortDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        val menuItem = menu.findItem(R.id.action_filter)
        val menuWishItem = menu.findItem(R.id.action_cart)
        if (mId != RECENTSEARCH) {
            menuItem.isVisible = true
        }
        menuWishItem.isVisible = true
        menuCart = menuWishItem.actionView
        menuCart?.findViewById<android.widget.ImageView>(R.id.ivCart)?.setColorFilter(requireActivity().color(R.color.textColorPrimary))
        menuWishItem.actionView?.onClick {
            activity?.launchActivity<MyCartActivity> { }
        }
        setCartCount()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                activity?.launchActivity<SearchActivity>()
                true
            }
            R.id.action_filter -> {
                if (mIsFilterDataLoaded) {
                    openFilterBottomSheet()
                } else {
                    toast(mProductAttributeResponseMsg)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun listFeaturedProducts(countLoadMore: Int = 1) {
        showProgress()
        mFilterProductRequest.page = countLoadMore
        callApi(getRestApis(false).getFeaturedProducts(mFilterProductRequest), onApiSuccess = {
            if (activity == null) return@callApi
            hideProgress()
            if (it.isEmpty()) {
                binding.rvNewestProduct.hide()
            } else {
                mIsLoading = it.size != 10
                if (mListAdapter.itemCount == 0 && mGridAdapter.itemCount == 0) {
                    binding.ivGrid.performClick()
                }
                if (countLoadMore == 1) {
                    mListAdapter.clearData()
                    mGridAdapter.clearData()
                }
                mListAdapter.addMoreItems(it)
                mGridAdapter.addMoreItems(it)
                if (mListAdapter.getModel().isEmpty() && mGridAdapter.getModel().isEmpty()) {
                    binding.noDataInclude.rlNoData.show()
                    binding.rvNewestProduct.hide()
                } else {
                    binding.noDataInclude.rlNoData.hide()
                    binding.rvNewestProduct.show()
                }
            }
        }, onApiError = {
            if (activity == null) return@callApi
            hideProgress()
            snackBar(it)
        }, onNetworkError = {
            if (activity == null) return@callApi
            hideProgress()
            activity?.noInternetSnackBar()
        })
    }

    private fun listAllProducts(mId: Int, countLoadMore: Int = 1) {
        binding.progressBar.show()
        val requestModel = RequestModel(); requestModel.page = countLoadMore
        callApi(getRestApis().listAllProducts(requestModel), onApiSuccess = {
            if (activity == null) return@callApi
            binding.progressBar.hide()
            when (mId) {
                NEWEST -> {
                    mIsLoading = it.size != 10
                    if (mListAdapter.itemCount == 0 && mGridAdapter.itemCount == 0) {
                        binding.ivGrid.performClick()
                    }
                    if (countLoadMore == 1) {
                        mListAdapter.clearData()
                        mGridAdapter.clearData()
                    }
                    mListAdapter.addMoreItems(it)
                    mGridAdapter.addMoreItems(it)
                    if (it.isNotEmpty()) {
                        binding.rvNewestProduct.show(); binding.noDataInclude.rlNoData.hide()
                    } else {
                        binding.noDataInclude.rlNoData.show(); binding.rvNewestProduct.hide()
                    }
                }
            }
        }, onApiError = {
            if (activity == null) return@callApi
            binding.progressBar.hide()
        }, onNetworkError = {
            if (activity == null) return@callApi
            binding.progressBar.hide()
        })
    }

    private fun getRecentItems(): ArrayList<ProductDataNew> {
        val list = recentProduct()
        list.reverse()
        return list
    }

}