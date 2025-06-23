package com.pharmacy.app.activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.WooBoxApp
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.databinding.ActivitySettingsBinding
import com.pharmacy.app.databinding.DialogLaunguageSelectionBinding
import com.pharmacy.app.fragments.DashboardListFragment
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.SharedPref.LANGUAGE
import com.pharmacy.app.utils.SLocaleHelper
import com.pharmacy.app.utils.extensions.*

var mIsLanguageUpdated = false

class SettingActivity : BaseViewBindingActivity<ActivitySettingsBinding>() {
    private lateinit var lan: String
    private var codes = arrayOf(
            "en",
            "hi",
            "fr",
            "es",
            "de",
            "in",
            "af",
            "pt",
            "tr",
            "ar",
            "vi",
            "ru"
    )
    private var mCountryImg = intArrayOf(
            R.drawable.us,
            R.drawable.india,
            R.drawable.france,
            R.drawable.spain,
            R.drawable.germany,
            R.drawable.indonesia,
            R.drawable.south_africa,
            R.drawable.portugal,
            R.drawable.turkey,
            R.drawable.ar,
            R.drawable.vietnam,
            R.drawable.russia

    )

    private var mIsSelectedByUser = false
    private var mDashboardListFragment: DashboardListFragment? = null

    override fun getViewBinding(): ActivitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_setting)
        setToolbar(binding.toolbarInclude.toolbar)
        binding.toolbarInclude.toolbar.setNavigationOnClickListener { onBackPressed() }

        lan = getAppLanguage()
        val languages = resources.getStringArray(R.array.language)
        binding.switchNightMode.isChecked = WooBoxApp.appTheme == Constants.THEME.DARK

        mIsLanguageUpdated = false

        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogLaunguageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        val languageAdapter = RecyclerViewAdapter<String>(R.layout.spinner_language, onBind = { view: View, s: String, i: Int ->
            view.findViewById<ImageView>(R.id.ivLogo).setImageResource(mCountryImg[i])
            view.findViewById<TextView>(R.id.tvName).text = languages[i]
        })
        languageAdapter.onItemClick = { i: Int, view: View, s: String ->
            binding.ivLanguage.loadImageFromDrawable(mCountryImg[i])
            binding.tvLanguage.text = languages[i]
            dialog.dismiss()
            setNewLocale(codes[i])
        }
        dialogBinding.listLanguage.apply {
            setVerticalLayout()
            adapter = languageAdapter
        }
        languageAdapter.addItems(languages.toCollection(ArrayList()))
        binding.llLanguage.onClick {
            dialog.show()
        }
        binding.llDashboard.onClick {
            if (mDashboardListFragment == null) {
                mDashboardListFragment = DashboardListFragment.newInstance()
            }
            if (mDashboardListFragment?.isAdded!!) {
                return@onClick
            }
            mDashboardListFragment?.show(supportFragmentManager, DashboardListFragment.tag)
        }
        codes.forEachIndexed { i: Int, s: String ->
            if (lan == s) {
                binding.ivLanguage.loadImageFromDrawable(mCountryImg[i])
                binding.tvLanguage.text = languages[i]
            }
        }
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            WooBoxApp.getAppInstance().enableNotification(isChecked)
        }
        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            WooBoxApp.changeAppTheme(isChecked)
            switchToDarkMode(isChecked)
        }
        Handler().postDelayed({ mIsSelectedByUser = true }, 1000)

    }

    override fun onBackPressed() {
        if (mIsLanguageUpdated) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
        /*if (lan != WooBoxApp.language) {
            launchActivityWithNewTask<DashBoardActivity>()
            exitProcess(0)
        } else {
            super.onBackPressed()
        }*/
    }

    private fun setNewLocale(language: String) {
        Log.e("lan", language)
        mIsLanguageUpdated = true
        getSharedPrefInstance().setValue(LANGUAGE, language)
        SLocaleHelper.setLocale(this, language);
        recreate()
    }
}


