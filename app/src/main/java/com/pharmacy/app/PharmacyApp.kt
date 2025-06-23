package com.pharmacy.app

import android.app.Dialog
import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

import com.pharmacy.app.network.RestApis
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.SharedPrefUtils
import com.pharmacy.app.utils.extensions.getSharedPrefInstance
import com.onesignal.OneSignal
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import okhttp3.OkHttpClient


class PharmacyApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        appInstance = this
        getSharedPrefInstance().apply {
            appTheme = getIntValue(Constants.SharedPref.THEME, Constants.THEME.LIGHT)
            // Set Russian as default language if no language is set
            if (getStringValue(Constants.SharedPref.LANGUAGE, "").isEmpty()) {
                setValue(Constants.SharedPref.LANGUAGE, "ru")
            }
        }

        // Set Custom Font

        ViewPump.init(ViewPump.builder().addInterceptor(CalligraphyInterceptor(CalligraphyConfig.Builder().setDefaultFontPath(getString(R.string.font_regular)).setFontAttrId(R.attr.fontPath).build())).build())


        // OneSignal Initialization
        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        
        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(getString(R.string.onesignal_app_id))
    }


    fun enableNotification(isEnabled: Boolean) {
        OneSignal.disablePush(!isEnabled)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        private lateinit var appInstance: PharmacyApp
        var restApis: RestApis? = null
        var okHttpClient: OkHttpClient? = null
        var sharedPrefUtils: SharedPrefUtils? = null
        var noInternetDialog: Dialog? = null
        var appTheme: Int = 0

        fun getAppInstance(): PharmacyApp {
            return appInstance
        }

        fun changeAppTheme(isDark: Boolean) {
            getSharedPrefInstance().apply {
                when {
                    isDark -> setValue(Constants.SharedPref.THEME, Constants.THEME.DARK)
                    else -> setValue(Constants.SharedPref.THEME, Constants.THEME.LIGHT)
                }
                appTheme = getIntValue(Constants.SharedPref.THEME, Constants.THEME.LIGHT)
            }

        }

    }
} 