package com.pharmacy.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pharmacy.app.R
import com.pharmacy.app.WooBoxApp
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.extensions.getSharedPrefInstance
import com.pharmacy.app.utils.extensions.launchActivity
import com.pharmacy.app.utils.extensions.runDelayed
import com.pharmacy.app.utils.extensions.switchToDarkMode
import com.pharmacy.app.utils.SLocaleHelper

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        switchToDarkMode(WooBoxApp.appTheme == Constants.THEME.DARK)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Ensure Russian locale is set
        SLocaleHelper.setLocale(this, "ru")

        runDelayed(1000) {
            if (getSharedPrefInstance().getBooleanValue(Constants.SharedPref.SHOW_SWIPE)) {
                launchActivity<DashBoardActivity>()
            } else {
                launchActivity<WalkThroughActivity>()
            }
            finish()
        }
    }
}