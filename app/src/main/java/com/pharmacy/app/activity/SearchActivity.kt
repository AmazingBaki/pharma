package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.fragments.SearchFragment
import com.pharmacy.app.utils.extensions.addFragment

class SearchActivity : AppBaseActivity() {

    private val mSearchFragment = SearchFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search2)
        addFragment(mSearchFragment, R.id.fragmentContainer)
    }

}