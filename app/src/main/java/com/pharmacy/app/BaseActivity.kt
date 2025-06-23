package com.pharmacy.app

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.pharmacy.app.PharmacyApp.Companion.noInternetDialog
import com.pharmacy.app.activity.DashBoardActivity
import com.pharmacy.app.utils.Constants.THEME.DARK
import com.pharmacy.app.utils.SLocaleHelper
import com.pharmacy.app.utils.extensions.changeToolbarFont
import com.pharmacy.app.utils.extensions.launchActivityWithNewTask
import com.pharmacy.app.utils.extensions.switchToDarkMode
import io.github.inflationx.viewpump.ViewPumpContextWrapper

import java.util.*

open class BaseActivity : AppCompatActivity() {
    private var progressDialog: Dialog? = null
    var language: Locale? = null
    private var themeApp: Int = 0


    fun setToolbarWithoutBackButton(mToolbar: Toolbar) {
        setSupportActionBar(mToolbar)
    }

    fun setToolbar(mToolbar: Toolbar) {
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace)
        mToolbar.changeToolbarFont()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        switchToDarkMode(PharmacyApp.appTheme == DARK)
        super.onCreate(savedInstanceState)
        noInternetDialog = null
        if (progressDialog == null) {
            progressDialog = Dialog(this)
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(0))
            progressDialog?.setContentView(R.layout.custom_dialog)
        }
        themeApp = PharmacyApp.appTheme
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showProgress(show: Boolean) {
        when {
            show -> {
                if (!isFinishing && !progressDialog!!.isShowing) {
                    progressDialog?.setCanceledOnTouchOutside(false)
                    progressDialog?.show()
                }
            }
            else -> try {
                if (progressDialog?.isShowing!! && !isFinishing) {
                    progressDialog?.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(SLocaleHelper.onAttach(newBase!!)))
    }

    override fun onStart() {
        Log.d("onStart", "called")
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        val appTheme = PharmacyApp.appTheme

        /*val locale = Locale(PharmacyApp.language)
        if (language != null && locale != language) {
            recreate()
            return
        }*/

        if (themeApp != 0 && themeApp != appTheme) {
            launchActivityWithNewTask<DashBoardActivity>()
        }

    }
} 