package com.pharmacy.app.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityBlogBinding
import com.pharmacy.app.databinding.ItemBlogBinding
import com.pharmacy.app.models.Blog
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.extensions.*

class BlogActivity : BaseViewBindingActivity<ActivityBlogBinding>() {

    private var countLoadMore = 1
    private lateinit var mBlogAdapter: BaseRecyclerAdapter<Blog, ItemBlogBinding>
    private var mOrderData = ArrayList<Blog>()

    var isLoading = false

    override fun getViewBinding(): ActivityBlogBinding {
        return ActivityBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.lbl_blog)
        setToolbar(binding.toolbarInclude.toolbar)

        mBlogAdapter = object : BaseRecyclerAdapter<Blog, ItemBlogBinding>() {
            override fun onItemLongClick(view: View, model: Blog, position: Int) {

            }

            override fun onItemClick(view: View, model: Blog, position: Int, dataBinding: ItemBlogBinding) {
                if (view.id == R.id.rlMainOrder) {
                    launchActivity<BlogDetailActivity>
                    {
                        putExtra(DATA, model)
                    }
                }
            }

            override val layoutResId: Int = R.layout.item_blog

            override fun onBindData(model: Blog, position: Int, dataBinding: ItemBlogBinding) {
                if (model.image!=null){
                    dataBinding.ivProduct.loadImageFromUrl(model.image)
                }
            }
        }

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvBlog.layoutManager = linearLayoutManager

        binding.rvBlog.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val countItem = linearLayoutManager.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                val isLastPosition = countItem.minus(1) == lastVisiblePosition

                if (!isLoading && isLastPosition) {
                    isLoading = true

                    binding.progressBar.show()
                    countLoadMore = countLoadMore.plus(1)
                    loadOrder(countLoadMore)
                }
            }
        })
        binding.rvBlog.adapter = mBlogAdapter
        loadOrder(countLoadMore)

    }

    private fun loadOrder(page: Int) {
        binding.progressBar.show()
        callApi(getRestApis().getBlogs(page), onApiSuccess = {
            isLoading = it.size != 10; mOrderData.addAll(it); binding.progressBar.hide()

            if (mOrderData.size == 0) binding.noDataInclude.rlNoData.show() else binding.noDataInclude.rlNoData.hide()
            mBlogAdapter.addItems(it)

        }, onApiError = {
            binding.progressBar.hide(); snackBarError(it)
        }, onNetworkError = {
            binding.progressBar.hide(); noInternetSnackBar()
        })
    }
}