package com.pharmacy.app.activity

import android.os.Bundle
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityEditProfileBinding
import com.pharmacy.app.fragments.ProfileFragment
import com.pharmacy.app.utils.extensions.addFragment

class EditProfileActivity : BaseViewBindingActivity<ActivityEditProfileBinding>() {

    override fun getViewBinding(): ActivityEditProfileBinding {
        return ActivityEditProfileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title=getString(R.string.lbl_edit_profile)
        addFragment(ProfileFragment(),R.id.profilecontainer)
    }
}
