package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityBlogDetailBinding
import com.pharmacy.app.models.Blog
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.extensions.loadImageFromUrl

class BlogDetailActivity : BaseViewBindingActivity<ActivityBlogDetailBinding>() {

    override fun getViewBinding(): ActivityBlogDetailBinding {
        return ActivityBlogDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar(binding.toolbarInclude.toolbar)

        val blog = intent.getSerializableExtra(DATA) as Blog
        title = blog.title

        binding.ivProduct.loadImageFromUrl(blog.image!!)
        binding.tvTitle.text = blog.title
        binding.tvPublishDate.text = blog.publish_date
        binding.tvDescription.text = blog.description
    }
}
