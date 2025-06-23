package com.pharmacy.app.activity

import android.os.Bundle

import com.pharmacy.app.R
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityAboutBinding
import com.pharmacy.app.utils.Constants.SharedPref.CONTACT
import com.pharmacy.app.utils.Constants.SharedPref.COPYRIGHT_TEXT
import com.pharmacy.app.utils.Constants.SharedPref.FACEBOOK
import com.pharmacy.app.utils.Constants.SharedPref.INSTAGRAM
import com.pharmacy.app.utils.Constants.SharedPref.PRIVACY_POLICY
import com.pharmacy.app.utils.Constants.SharedPref.TERM_CONDITION
import com.pharmacy.app.utils.Constants.SharedPref.TWITTER
import com.pharmacy.app.utils.Constants.SharedPref.WHATSAPP
import com.pharmacy.app.utils.extensions.*

class AboutActivity : BaseViewBindingActivity<ActivityAboutBinding>() {
    private var whatsUp: String = ""
    private var instagram: String = ""
    private var twitter: String = ""
    private var facebook: String = ""
    private var contact: String = ""
    private var copyRight: String = ""
    private var privacy: String = ""
    private var toc: String = ""

    override fun getViewBinding(): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.lbl_about)

        getSharedPrefInstance().apply {
            whatsUp = getStringValue(WHATSAPP)
            instagram = getStringValue(INSTAGRAM)
            twitter = getStringValue(TWITTER)
            facebook = getStringValue(FACEBOOK)
            contact = getStringValue(CONTACT)
            copyRight = getStringValue(COPYRIGHT_TEXT)
            privacy = getStringValue(PRIVACY_POLICY)
            toc = getStringValue(TERM_CONDITION)
        }
        if (copyRight.isEmpty()) {
            binding.tvCopyRight.hide()
        } else {
            binding.tvCopyRight.text = copyRight
            binding.tvCopyRight.show()
        }

        if (whatsUp.isEmpty()) binding.ivWhatsapp.hide()
        if (privacy.isEmpty()) binding.tvPrivacyPolicy.hide()
        if (toc.isEmpty()) binding.tvTOC.hide()
        if (instagram.isEmpty()) binding.ivInstagram.hide()
        if (twitter.isEmpty()) binding.ivTwitterSign.hide()
        if (facebook.isEmpty()) binding.ivFacebook.hide()
        if (contact.isEmpty()) binding.ivContact.hide()
        binding.llBottom.show()

        binding.ivWhatsapp.onClick { openCustomTab("https://wa.me/${whatsUp}") }
        binding.ivInstagram.onClick { openCustomTab(instagram) }
        binding.ivTwitterSign.onClick { openCustomTab(twitter) }
        binding.ivFacebook.onClick { openCustomTab(facebook) }
        binding.ivContact.onClick { dialNumber(contact) }
        binding.tvPrivacyPolicy.onClick { openCustomTab(privacy) }
        binding.tvTOC.onClick { openCustomTab(toc) }
    }
}
