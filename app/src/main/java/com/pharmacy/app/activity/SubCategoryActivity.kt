package com.pharmacy.app.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.databinding.ActivitySubCategoryBinding
import com.pharmacy.app.fragments.ViewAllProductFragment
import com.pharmacy.app.models.CategoryData
import com.pharmacy.app.models.FilterProductRequest
import com.pharmacy.app.models.ProductDataNew
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.KeyIntent.KEYID
import com.pharmacy.app.utils.Constants.KeyIntent.TITLE
import com.pharmacy.app.utils.Constants.KeyIntent.VIEWALLID
import com.pharmacy.app.utils.Constants.ViewAllCode.CATEGORY_FEATURED
import com.pharmacy.app.utils.extensions.*

class SubCategoryActivity : BaseViewBindingActivity<ActivitySubCategoryBinding>() {
    private lateinit var mCategoryData: CategoryData
    private var imgLayoutParams: LinearLayout.LayoutParams? = null
    private lateinit var mFeaturedAdapter: RecyclerViewAdapter<ProductDataNew>
    private lateinit var mNewArrivalAdapter: RecyclerViewAdapter<ProductDataNew>
    private var mViewAllProductFragment = ViewAllProductFragment()

    override fun getViewBinding(): ActivitySubCategoryBinding = ActivitySubCategoryBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getSerializableExtra(DATA) == null) {
            toast(R.string.error_something_went_wrong)
            finish()
            return
        }
        mCategoryData = intent.getSerializableExtra(DATA) as CategoryData

        setToolbar(binding.toolbarLay.toolbar)
        title = mCategoryData.name.getHtmlString()
        imgLayoutParams = productLayoutParams()
        binding.rcvNewArrival.setHorizontalLayout()
        binding.rcvPopular.setHorizontalLayout()
        mFeaturedAdapter = getFeaturedAdapter()
        mNewArrivalAdapter = getFeaturedAdapter()
        binding.rcvNewArrival.adapter = mNewArrivalAdapter
        binding.rcvPopular.adapter = mFeaturedAdapter
        mNewArrivalAdapter.setModelSize(5)
        mFeaturedAdapter.setModelSize(5)
        mFeaturedAdapter.onItemClick = { pos, view, item ->
            showProductDetail(item)
        }
        mNewArrivalAdapter.onItemClick = { pos, view, item ->
            showProductDetail(item)
        }

        binding.viewNewArrival.root.onClick {
            launchActivity<ViewAllProductActivity> {
                putExtra(TITLE, getString(R.string.lbl_new_arrival))
                putExtra(VIEWALLID, Constants.ViewAllCode.CATEGORY_NEWEST)
                putExtra(KEYID, mCategoryData.cat_ID)
            }
        }
        binding.viewFeatured.root.onClick {
            launchActivity<ViewAllProductActivity> {
                putExtra(TITLE, getString(R.string.lbl_popular))
                putExtra(VIEWALLID, CATEGORY_FEATURED)
                putExtra(KEYID, mCategoryData.cat_ID)
            }
        }
        getSubCategoryProducts()
        getFeaturedProducts()
        if (mCategoryData.subcategory?.isNotEmpty()!!) {
            setupSubCategory()
            binding.rcvSubCategory.show()
        } else {
            binding.rcvSubCategory.hide()
        }
    }

    private fun setupSubCategory() {
        showProgress(true)
        val requestModel = RequestModel(); requestModel.cat_id = mCategoryData.cat_ID

        callApi(getRestApis(false).getSubCategories(requestModel), onApiSuccess = {
            showProgress(false)
            val subCategoryAdapter = getCategoryAdapter()
            binding.rcvSubCategory.apply { setHorizontalLayout(); adapter = subCategoryAdapter }
            subCategoryAdapter.addItems(it)
        }, onApiError = {
            showProgress(false)
            snackBarError(it)
        }, onNetworkError = {
            showProgress(false)
            noInternetSnackBar()
        })
    }

    private fun getFeaturedProducts() {
        showProgress(true)
        val mSelectedCategory: ArrayList<Int> = ArrayList()
        mSelectedCategory.add(mCategoryData.cat_ID)
        val requestModel = FilterProductRequest(); requestModel.category = mSelectedCategory
        callApi(getRestApis(false).getFeaturedProducts(requestModel), onApiSuccess = {
            showProgress(false)
            if (it.size == 0) {
                binding.rlFeature.hide()
                binding.rcvPopular.hide()
            } else {
                binding.rlFeature.show()

                mFeaturedAdapter.addItems(it)
            }
        }, onApiError = {
            binding.rlFeature.hide()
            binding.rcvPopular.hide()
            showProgress(false)
            if (it.contains("Sorry! No Product Available")){
                snackBarError("Sorry! No featured Product Available")
            }else{
                snackBarError(it)
            }
        }, onNetworkError = {
            showProgress(false)
            noInternetSnackBar()
        })
    }

    private fun getSubCategoryProducts() {
        showProgress(true)
        val mFilterProductRequest = FilterProductRequest()

        val mSelectedCategory: ArrayList<Int> = ArrayList()
        mSelectedCategory.add(mCategoryData.cat_ID)

        if (mSelectedCategory.isNotEmpty()) mFilterProductRequest.category = mSelectedCategory

        callApi(getRestApis().filterProduct(request = mFilterProductRequest), onApiSuccess = {
            showProgress(false)
            if (it.size == 0) {
                binding.rlNewArrival.hide()
                binding.rcvNewArrival.hide()
            } else {
                binding.rlNewArrival.show()
                mNewArrivalAdapter.addItems(it)
            }
            showProgress(false)
        }, onApiError = {
            binding.rlNewArrival.hide()
            binding.rcvNewArrival.hide()
            showProgress(false)
            if (it.contains("Sorry! No Product Available")){
                snackBarError("Sorry! No Newest Product Available")
            }else{
                snackBarError(it)
            }
        }, onNetworkError = {
            showProgress(false)
            openLottieDialog {
                getSubCategoryProducts()
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.getItem(0)?.isVisible = !mViewAllProductFragment.isVisible
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            loadSearchFragment()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun loadSearchFragment() {
        launchActivity<SearchActivity>()
    }

    override fun onBackPressed() {
        when {
            mViewAllProductFragment.isVisible -> {
                title = intent.getStringExtra(TITLE)
                removeFragment(mViewAllProductFragment)
                invalidateOptionsMenu()
            }
            else -> super.onBackPressed()
        }
    }

    private fun getFeaturedAdapter(): RecyclerViewAdapter<ProductDataNew> {
        return RecyclerViewAdapter(
            R.layout.item_product_new,
            onBind = { view, item, position -> setProductItem(view, item) })
    }


}