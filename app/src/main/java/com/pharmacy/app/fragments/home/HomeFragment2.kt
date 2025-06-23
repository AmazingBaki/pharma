package com.pharmacy.app.fragments.home

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pharmacy.app.R
import com.pharmacy.app.activity.*
import com.pharmacy.app.adapter.HomeSliderAdapter
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ItemCategory2Binding
import com.pharmacy.app.fragments.BaseViewBindingFragment
import com.pharmacy.app.models.*
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.KeyIntent.TITLE
import com.pharmacy.app.utils.Constants.KeyIntent.VIEWALLID
import com.pharmacy.app.utils.Constants.SharedPref.CATEGORY_DATA
import com.pharmacy.app.utils.Constants.SharedPref.CONTACT
import com.pharmacy.app.utils.Constants.SharedPref.COPYRIGHT_TEXT
import com.pharmacy.app.utils.Constants.SharedPref.DEFAULT_CURRENCY
import com.pharmacy.app.utils.Constants.SharedPref.FACEBOOK
import com.pharmacy.app.utils.Constants.SharedPref.INSTAGRAM
import com.pharmacy.app.utils.Constants.SharedPref.KEY_ORDER_COUNT
import com.pharmacy.app.utils.Constants.SharedPref.PRIVACY_POLICY
import com.pharmacy.app.utils.Constants.SharedPref.SLIDER_IMAGES_DATA
import com.pharmacy.app.utils.Constants.SharedPref.TERM_CONDITION
import com.pharmacy.app.utils.Constants.SharedPref.THEME_COLOR
import com.pharmacy.app.utils.Constants.SharedPref.TWITTER
import com.pharmacy.app.utils.Constants.SharedPref.WHATSAPP
import com.pharmacy.app.utils.Constants.ViewAllCode.FEATURED
import com.pharmacy.app.utils.Constants.ViewAllCode.NEWEST
import com.pharmacy.app.utils.Constants.ViewAllCode.RECENTSEARCH
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentHome2Binding


class HomeFragment2 : BaseViewBindingFragment<FragmentHome2Binding>() {

    //region Variables
    private var imgLayoutParams: LinearLayout.LayoutParams? = null
    private var mCategoryAdapter: BaseRecyclerAdapter<CategoryData, ItemCategory2Binding>? = null
    private var mFeaturedProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mNewArrivalProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mOfferProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mYouMayLikeProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mDealProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mSuggestedProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mRecentProductAdapter: RecyclerViewAdapter<ProductDataNew>? = null
    private var mTestimonialsAdapter: RecyclerViewAdapter<Testimonials>? = null

    var onNetworkRetry: (() -> Unit)? = null
    //endregion

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHome2Binding {
        return FragmentHome2Binding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        imgLayoutParams = requireActivity().productLayoutParams()
        mCategoryAdapter = requireActivity().getCategory2Adapter()

        binding.rcvCategory.apply {
            layoutManager = GridLayoutManager(activity, 2, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = mCategoryAdapter
            rvItemAnimation()
        }
        binding.rcvRecentSearch.setHorizontalLayout(); binding.rcvNewestProduct.setHorizontalLayout(); binding.rcvFeaturedProducts.setHorizontalLayout()
        binding.rcvDealProducts.setHorizontalLayout(); binding.rcvYouMayLikeProducts.setHorizontalLayout(); binding.rcvOfferProducts.setHorizontalLayout(); binding.rcvSuggestedProducts.setHorizontalLayout()
        binding.rcvTestimonials.setHorizontalLayout()

        setClickEventListener()

        setupRecentProductAdapter(); setupNewArrivalProductAdapter(); setupFeaturedProductAdapter();setTestimonialAdapter()

        setupOfferProductAdapter(); setupSuggestedProductAdapter(); setupYouMayLikeProductAdapter(); setupDealProductAdapter()

        loadApis()
        binding.refreshLayout.setOnRefreshListener {
            loadApis()
            binding.refreshLayout.isRefreshing = false
        }
    }


    //region APIs
    private fun loadApis() {
        if (isNetworkAvailable()) {
            listAllProducts(); listAllProductCategories(); getSliders();listFeaturedProducts()
        } else {
            listAllProductCategories(); getSliders()
            activity?.openLottieDialog { loadApis(); onNetworkRetry?.invoke() }
        }
    }

    private fun getSliders() {
        val images = getSlideImagesFromPref()
        val sliderImagesAdapter = HomeSliderAdapter(requireActivity(), images)
        binding.homeSlider.adapter = sliderImagesAdapter
        binding.dots.attachViewPager(binding.homeSlider)
        binding.dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)
        if (images.isNotEmpty()) {
            binding.rlHead.show()
        } else {
            binding.rlHead.hide()
        }

        callApi(getRestApis(false).getSliderImages(), onApiSuccess = { res ->
            if (activity == null) return@callApi
            getSharedPrefInstance().setValue(SLIDER_IMAGES_DATA, Gson().toJson(res))
            images.clear()
            images.addAll(getSlideImagesFromPref())
            binding.dots.attachViewPager(binding.homeSlider)
            binding.dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)
            sliderImagesAdapter.notifyDataSetChanged()
            if (images.isNotEmpty()) {
                binding.rlHead.show()
            } else {
                binding.rlHead.hide()
            }
        }, onApiError = {
            if (activity == null) return@callApi
            binding.rlHead.hide()
        }, onNetworkError = {
            if (activity == null) return@callApi
            activity?.noInternetSnackBar()
            binding.rlHead.hide()
        })
    }

    private fun listAllProducts() {
        showProgress()
        val requestModel = RequestModel()
        if (isLoggedIn()) requestModel.user_id = getUserId()

        callApi(getRestApis(false).dashboard(requestModel), onApiSuccess = {
            if (activity == null) return@callApi
            hideProgress()

            getSharedPrefInstance().apply {
                removeKey(WHATSAPP)
                removeKey(FACEBOOK)
                removeKey(TWITTER)
                removeKey(INSTAGRAM)
                removeKey(CONTACT)
                removeKey(PRIVACY_POLICY)
                removeKey(TERM_CONDITION)
                removeKey(COPYRIGHT_TEXT)
                setValue(DEFAULT_CURRENCY, it.currency_symbol.currency_symbol)
                setValue(KEY_ORDER_COUNT, it.total_order)
                setValue(THEME_COLOR, it.theme_color)
                setValue(WHATSAPP, it.social_link?.whatsapp)
                setValue(FACEBOOK, it.social_link?.facebook)
                setValue(TWITTER, it.social_link?.twitter)
                setValue(INSTAGRAM, it.social_link?.instagram)
                setValue(CONTACT, it.social_link?.contact)
                setValue(PRIVACY_POLICY, it.social_link?.privacy_policy)
                setValue(TERM_CONDITION, it.social_link?.term_condition)
                setValue(COPYRIGHT_TEXT, it.social_link?.copyright_text)
            }
            (activity as DashBoardActivity).setOrderCount();
            if (it.newest.isEmpty()) {
                binding.rlNewestProduct.hide()
                binding.rcvNewestProduct.hide()
            } else {
                binding.rlNewestProduct.show()
                binding.rcvNewestProduct.show()
                mNewArrivalProductAdapter?.addItems(it.newest)
            }
            if (it.featured.isEmpty()) {
                binding.rlFeatured.hide()
                binding.rcvFeaturedProducts.hide()
            } else {
                binding.rlFeatured.show()
                binding.rcvFeaturedProducts.show()
                mFeaturedProductAdapter?.addItems(it.featured)
            }
            if (it.testimonials.isEmpty()) {
                binding.rlTestimonials.hide()
                binding.rcvTestimonials.hide()
            } else {
                binding.rlTestimonials.show()
                binding.rcvTestimonials.show()
                mTestimonialsAdapter?.addItems(it.testimonials)
            }

            if (it.deal_product.isEmpty()) {
                binding.rlDeal.hide()
                binding.rcvDealProducts.hide()
            } else {
                binding.rlDeal.show()
                binding.rcvDealProducts.show()
                mDealProductAdapter?.addItems(it.deal_product)
            }
            if (it.you_may_like.isEmpty()) {
                binding.rlYouMayLike.hide()
                binding.rcvYouMayLikeProducts.hide()
            } else {
                binding.rlYouMayLike.show()
                binding.rcvYouMayLikeProducts.show()
                mYouMayLikeProductAdapter?.addItems(it.newest)
            }
            if (it.offer.isEmpty()) {
                binding.rlOffer.hide()
                binding.rcvOfferProducts.hide()
            } else {
                binding.rlOffer.show()
                binding.rcvOfferProducts.show()
                mOfferProductAdapter?.addItems(it.offer)
            }
            if (it.suggested_product.isEmpty()) {
                binding.rlSuggested.hide()
                binding.rcvSuggestedProducts.hide()
            } else {
                binding.rlSuggested.show()
                binding.rcvSuggestedProducts.show()
                mSuggestedProductAdapter?.addItems(it.suggested_product)
            }

            if (it.banner_1 != null && it.banner_1.url.isNotEmpty()) {
                binding.ivBanner1.show(); binding.ivBanner1.loadImageFromUrl(it.banner_1.image); binding.ivBanner1.setOnClickListener { _ ->
                    activity?.openCustomTab(
                        it.banner_1.url
                    )
                }
            } else {
                binding.ivBanner1.hide()
            }
            if (it.banner_2 != null && it.banner_2.url.isNotEmpty()) {
                binding.ivBanner2.show(); binding.ivBanner2.loadImageFromUrl(it.banner_2.image); binding.ivBanner2.setOnClickListener { _ ->
                    activity?.openCustomTab(
                        it.banner_2.url
                    )
                }
            } else {
                binding.ivBanner2.hide()
            }
            if (it.banner_3 != null && it.banner_3.url.isNotEmpty()) {
                binding.ivBanner3.show(); binding.ivBanner3.loadImageFromUrl(it.banner_3.image); binding.ivBanner3.setOnClickListener { _ ->
                    activity?.openCustomTab(
                        it.banner_3.url
                    )
                }
            } else {
                binding.ivBanner3.hide()
            }

            // Setup recent search products from local storage
            val recentItems = getRecentItems()
            if (recentItems.isEmpty()) {
                binding.rlRecentSearch.hide()
                binding.rcvRecentSearch.hide()
            } else {
                binding.rlRecentSearch.show()
                binding.rcvRecentSearch.show()
                mRecentProductAdapter?.addItems(recentItems)
            }

        }, onApiError = {
            if (activity == null) return@callApi
            hideProgress()
            activity?.snackBarError(it)
        }, onNetworkError = {
            if (activity == null) return@callApi
            hideProgress()
            activity?.openLottieDialog { loadApis(); onNetworkRetry?.invoke() }
        })
    }

    private fun listAllProductCategories() {
        if (getCategoryDataFromPref().isEmpty()) {
            activity?.getAlertDialog(getString(R.string.error_something_went_wrong), onPositiveClick = { _, _ ->
                activity?.launchActivity<DashBoardActivity>()
            }, onNegativeClick = { _, _ ->
                // Do nothing
            })?.show()
        } else {
            mCategoryAdapter?.addItems(getCategoryDataFromPref())
        }
    }

    private fun listFeaturedProducts() {
        showProgress()
        val requestModel = FilterProductRequest()
        callApi(getRestApis(false).getFeaturedProducts(requestModel), onApiSuccess = {
            if (activity == null) return@callApi
            hideProgress()
            if (it.isEmpty()) {
                binding.rlFeatured.hide()
                binding.rcvFeaturedProducts.hide()
            } else {
                binding.rlFeatured.show()
                binding.rcvFeaturedProducts.show()
                mFeaturedProductAdapter?.addItems(it)
                mFeaturedProductAdapter?.setModelSize(5)
            }
        }, onApiError = {
            if (activity == null) return@callApi
            hideProgress()
        }, onNetworkError = {
            if (activity == null) return@callApi
            hideProgress()
            activity?.noInternetSnackBar()
        })
    }

    //endregion

    //region Private Methods
    private fun setClickEventListener() {
        binding.viewFeatured.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(VIEWALLID, FEATURED); putExtra(TITLE, getString(R.string.lbl_Featured)) } }
        binding.viewNewest.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(VIEWALLID, NEWEST); putExtra(TITLE, getString(R.string.lbl_newest_product)) } }
        binding.viewRecentSearch.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(VIEWALLID, RECENTSEARCH); putExtra(TITLE, getString(R.string.lbl_recent_search)) } }
        binding.viewYouMayLike.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(DATA, ArrayList(recentProduct())) } }
        binding.viewOffer.root.setOnClickListener { activity?.launchActivity<OfferActivity>() }
        binding.viewSuggested.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(DATA, ArrayList(recentProduct())) } }
        binding.viewDeal.root.setOnClickListener { activity?.launchActivity<ViewAllProductActivity> { putExtra(DATA, ArrayList(recentProduct())) } }
    }

    //region Adapter Methods
    private fun setupRecentProductAdapter() {
        mRecentProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvRecentSearch.adapter = mRecentProductAdapter

        mRecentProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupNewArrivalProductAdapter() {
        mNewArrivalProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvNewestProduct.adapter = mNewArrivalProductAdapter

        mNewArrivalProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupFeaturedProductAdapter() {
        mFeaturedProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvFeaturedProducts.adapter = mFeaturedProductAdapter

        mFeaturedProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupOfferProductAdapter() {
        mOfferProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvOfferProducts.adapter = mOfferProductAdapter

        mOfferProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupSuggestedProductAdapter() {
        mSuggestedProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvSuggestedProducts.adapter = mSuggestedProductAdapter

        mSuggestedProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupYouMayLikeProductAdapter() {
        mYouMayLikeProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvYouMayLikeProducts.adapter = mYouMayLikeProductAdapter

        mYouMayLikeProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setupDealProductAdapter() {
        mDealProductAdapter = RecyclerViewAdapter(R.layout.item_product_new, onBind = { view, item, position -> setProductItem(view, item) })
        binding.rcvDealProducts.adapter = mDealProductAdapter

        mDealProductAdapter?.onItemClick = { pos, view, item ->
            addToRecentProduct(item)
            (activity as DashBoardActivity).showProductDetail(item)
        }
    }

    private fun setTestimonialAdapter() {
        mTestimonialsAdapter = RecyclerViewAdapter(R.layout.item_testimonial, onBind = { view, item, position ->
            if(item.image != null) {
                view.findViewById<ImageView>(R.id.ivAuthor).loadImageFromUrl(item.image!!)
            } else {
                view.findViewById<ImageView>(R.id.ivAuthor).loadImageFromUrl("")
            }
            view.findViewById<TextView>(R.id.tvName).text = item.name
            view.findViewById<TextView>(R.id.tvDesignation).text = ""
            view.findViewById<TextView>(R.id.tvDesignation).text = item.designation
            if (item.company != null && item.company.isNotEmpty()) {
                view.findViewById<TextView>(R.id.tvDesignation).append(", " + item.company)
            }
            view.findViewById<TextView>(R.id.tvDescription).text = "\"" + item.message + "\""
        })
        binding.rcvTestimonials.adapter = mTestimonialsAdapter
    }

    //endregion

    //region Common
    private fun getRecentItems(): ArrayList<ProductDataNew> {
        val list = recentProduct(); list.reverse(); return list
    }
    //endregion

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                activity?.launchActivity<SearchActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //endregion
}