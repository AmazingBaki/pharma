package com.pharmacy.app.activity

import android.app.Activity
import android.os.Bundle
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityAccountBinding
import com.pharmacy.app.utils.extensions.*

class AccountActivity : BaseViewBindingActivity<ActivityAccountBinding>() {

    override fun getViewBinding(): ActivityAccountBinding {
        return ActivityAccountBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.title_account)

        binding.txtDisplayName.text = getUserFullName()
        binding.ivProfileImage.loadImageFromUrl(getUserProfile(),aPlaceHolderImage = R.drawable.ic_profile)
        binding.btnSignOut.onClick {
            val dialog = getAlertDialog(getString(R.string.lbl_logout_confirmation), onPositiveClick = { dialog, i ->
                clearLoginPref()
                launchActivityWithNewTask<DashBoardActivity>()
            }, onNegativeClick = { dialog, i ->
                dialog.dismiss()
            })
            dialog.show()
        }
        binding.tvOrders.onClick { launchActivity<OrderActivity>() }
        binding.tvOffer.onClick { launchActivity<OfferActivity>() }
        binding.ivProfileImage.onClick { launchActivity<EditProfileActivity>() }
        binding.tvHelpCenter.onClick { launchActivity<HelpActivity>() }
        binding.btnVerify.onClick { launchActivity<OTPActivity>() }
        binding.tvAddressManager.onClick {
            if (getAddressList().size == 0) {
                launchActivity<AddAddressActivity>()
            } else {
                launchActivity<AddressManagerActivity>()
            }
        }
        binding.tvWishlist.onClick {
            setResult(Activity.RESULT_OK)
            finish()
        }

    }
}