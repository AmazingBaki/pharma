package com.pharmacy.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityDashboardBinding
import com.pharmacy.app.fragments.MyCartFragment
import com.pharmacy.app.fragments.ProfileFragment
import com.pharmacy.app.fragments.WishListFragment
import com.pharmacy.app.fragments.home.HomeFragment
import com.pharmacy.app.fragments.home.HomeFragment2
import com.pharmacy.app.models.CategoryData
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.pharmacy.app.utils.Constants.AppBroadcasts.ORDER_COUNT_CHANGE
import com.pharmacy.app.utils.Constants.AppBroadcasts.PROFILE_UPDATE
import com.pharmacy.app.utils.Constants.AppBroadcasts.WISHLIST_UPDATE
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.SharedPref.KEY_ORDER_COUNT
import com.pharmacy.app.utils.Constants.SharedPref.KEY_WISHLIST_COUNT
import com.pharmacy.app.utils.SLocaleHelper
import com.pharmacy.app.utils.extensions.*

class DashBoardActivity : BaseViewBindingActivity<ActivityDashboardBinding>() {

    private var selectedDashboard: Int = 0

    //region Variables
    private lateinit var mHomeFragment: Fragment
    private var count: String = ""
    private val mWishListFragment = WishListFragment()
    private val mCartFragment = MyCartFragment()
    private val mProfileFragment = ProfileFragment()
    var selectedFragment: Fragment? = null
    //endregion

    override fun getViewBinding(): ActivityDashboardBinding {
        return ActivityDashboardBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(R.id.container) != null) {
            supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.container)!!).commit()
        }
        selectedDashboard = getSharedPrefInstance().getIntValue(Constants.SharedPref.KEY_DASHBOARD, 0)
        if (selectedDashboard == 0) {
            mHomeFragment = HomeFragment()
        } else if (selectedDashboard == 1) {
            mHomeFragment = HomeFragment2()
        }

        setToolbar(binding.main.toolbarInclude.toolbar); setUpDrawerToggle(); loadHomeFragment(); setListener()

        if (isLoggedIn()) {
            loadApis()
            setWishCount(); setCartCountFromPref()
            binding.sidebarInclude.llInfo.show()
            binding.sidebarInclude.tvLogout.text = getString(R.string.lbl_logout)
            binding.sidebarInclude.tvLogout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_logout, 0, 0, 0)
        } else {
            binding.sidebarInclude.tvLogout.text = getString(R.string.lbl_sign_in)
            binding.sidebarInclude.tvLogout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_login, 0, 0, 0)
        }

        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCountFromPref()
                if (mCartFragment.isAdded) mCartFragment.invalidateCartLayout(getCartListFromPref())
            }
            onAction(ORDER_COUNT_CHANGE) { setOrderCount() }
            onAction(PROFILE_UPDATE) { setUserInfo() }
            onAction(WISHLIST_UPDATE) { setWishCount() }
        }

        setUserInfo(); binding.sidebarInclude.tvVersionCode.text = String.format("%S %S", "V", getAppVersionName())
    }

    override fun onResume() {
        super.onResume()
    }

    private fun loadApis() {
        if (isNetworkAvailable()) {
            /*getOrderData();*/ fetchAndStoreCartData(); fetchAndStoreWishListData(); fetchAndStoreAddressData()
        }
    }

    //region Clicks
    private fun setListener() {
        binding.sidebarInclude.civProfile.onClick {
            if (isLoggedIn()) {
                launchActivity<EditProfileActivity>()
            } else {
                launchActivity<SignInUpActivity>()
            }
            closeDrawer()
        }
        binding.main.bottomBarInclude.llHome.onClick {
            closeDrawer()
            enable(binding.main.bottomBarInclude.ivHome)
            loadFragment(mHomeFragment)
            title = getString(R.string.home)
        }

        binding.main.bottomBarInclude.llWishList.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>(); return@onClick
            }
            closeDrawer()
            enable(binding.main.bottomBarInclude.ivWishList)
            loadFragment(mWishListFragment)
            title = getString(R.string.lbl_wish_list)
        }
        binding.sidebarInclude.llWishlistData.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>(); return@onClick
            }
            closeDrawer()
            loadWishListFragment()
        }

        binding.main.bottomBarInclude.llCart.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>(); return@onClick
            }
            closeDrawer()
            enable(binding.main.bottomBarInclude.menuCartInclude.ivCart)
            binding.main.bottomBarInclude.menuCartInclude.tvNotificationCount.hide()
            loadFragment(mCartFragment)
            if (mCartFragment.isAdded) {
                mCartFragment.invalidateCartLayout(getCartListFromPref())
            }
            title = getString(R.string.cart)
        }

        binding.main.bottomBarInclude.llProfile.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>(); return@onClick
            }
            closeDrawer()
            enable(binding.main.bottomBarInclude.ivProfile)
            loadFragment(mProfileFragment)
            title = getString(R.string.profile)
        }
        binding.sidebarInclude.tvAccount.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>()
            } else {
                launchActivity<AccountActivity>(Constants.RequestCode.ACCOUNT)
            }
            closeDrawer()
        }
//        binding.sidebarInclude.tvSettings.onClick {
//            launchActivity<SettingActivity>(requestCode = Constants.RequestCode.SETTINGS)
//            closeDrawer()
//        }
        //binding.sidebarInclude.tvBlog.onClick {
        //    launchActivity<BlogActivity>()
        //    closeDrawer()
        //}
        binding.sidebarInclude.tvHelp.onClick { launchActivity<HelpActivity>(); closeDrawer() }
        binding.sidebarInclude.tvFaq.onClick { launchActivity<FAQActivity>(); closeDrawer() }
        binding.sidebarInclude.tvContactus.onClick { launchActivity<ContactUsActivity>(); closeDrawer() }
        binding.sidebarInclude.tvAbout.onClick { launchActivity<AboutActivity>(); closeDrawer() }
        binding.sidebarInclude.ivCloseDrawer.onClick { closeDrawer() }
        binding.sidebarInclude.tvLogout.onClick {
            if (isLoggedIn()) {
                val dialog = getAlertDialog(
                        getString(R.string.lbl_logout_confirmation),
                        onPositiveClick = { _, _ ->
                            clearLoginPref()
                            launchActivityWithNewTask<DashBoardActivity>()
                            //recreate()
                        },
                        onNegativeClick = { dialog, _ ->
                            dialog.dismiss()
                        })
                dialog.show()
                closeDrawer()
            } else {
                launchActivity<SignInUpActivity>()
            }
        }
        binding.sidebarInclude.llOrders.onClick {
            if (isLoggedIn()) {
                launchActivity<OrderActivity>()
            } else {
                launchActivity<SignInUpActivity>()
            }
            closeDrawer()
        }
            //binding.sidebarInclude.tvShareApp.onClick { closeDrawer(); shareMyApp(this@DashBoardActivity, "", "") }
        binding.sidebarInclude.tvLblOffer.onClick { closeDrawer(); launchActivity<OfferActivity>() }
    }
    //endregion

    //region Set Data
    private fun showCartCount() {
        if (isLoggedIn() && !count.checkIsEmpty() && !count.equals("0", false)) {
            binding.main.bottomBarInclude.menuCartInclude.tvNotificationCount.show()
        } else {
            binding.main.bottomBarInclude.menuCartInclude.tvNotificationCount.hide()
        }
    }

    public fun setOrderCount() {
        binding.sidebarInclude.tvOrderCount.text = getSharedPrefInstance().getIntValue(KEY_ORDER_COUNT, 0).toDecimalFormat()
    }

    /*private fun getOrderData() {
        getOrders { setOrderCount() }
    }*/

    private fun setCartCountFromPref() {
        count = getCartCount()
        binding.main.bottomBarInclude.menuCartInclude.tvNotificationCount.text = count
        showCartCount()
        if (mCartFragment.isVisible) binding.main.bottomBarInclude.menuCartInclude.tvNotificationCount.hide()
    }

    private fun setUserInfo() {
        binding.sidebarInclude.txtDisplayName.text = getUserFullName()
        changeProfile()
    }

    private fun setWishCount() {
        binding.sidebarInclude.tvWishListCount.text =
                getSharedPrefInstance().getIntValue(KEY_WISHLIST_COUNT, 0).toDecimalFormat()
        if (mWishListFragment.isAdded) mWishListFragment.wishListItemChange()
    }

    //endregion

    //region Fragment Setups
    private fun loadWishListFragment() {
        enable(binding.main.bottomBarInclude.ivWishList)
        loadFragment(mWishListFragment)
        title = getString(R.string.lbl_wish_list)
    }

    fun setDrawerCategory(it: ArrayList<CategoryData>) {
        binding.sidebarInclude.rvCategory.create(it.size, R.layout.item_navigation_category, it, getVerticalLayout(false), onBindView = { item, position ->
            findViewById<android.widget.TextView>(R.id.tvCategory).text = item.name.getHtmlString()
            if (item.image != null) {
                findViewById<android.widget.ImageView>(R.id.ivCat).loadImageFromUrl(
                        item.image,
                        aPlaceHolderImage = R.drawable.cat_placeholder
                )
            }
        }, itemClick = { item, position ->
            closeDrawer()
            launchActivity<SubCategoryActivity> { putExtra(DATA, item) }
        })
        binding.sidebarInclude.rvCategory.isNestedScrollingEnabled = false
    }

    private fun loadFragment(aFragment: Fragment) {
        if (selectedFragment != null) {
            if (selectedFragment == aFragment) return
            hideFragment(selectedFragment!!)
        }
        if (aFragment.isAdded) {
            showFragment(aFragment)
        } else {

            addFragment(aFragment, R.id.container)
        }
        selectedFragment = aFragment
    }

    fun loadHomeFragment() {
        enable(binding.main.bottomBarInclude.ivHome)
        //if (!mHomeFragment.isAdded) loadFragment(mHomeFragment) else showFragment(mHomeFragment)
        loadFragment(mHomeFragment)
        title = getString(R.string.home)
        if (mHomeFragment is HomeFragment) {
            (mHomeFragment as HomeFragment).onNetworkRetry = { loadApis() }
        } else if (mHomeFragment is HomeFragment2) {
            (mHomeFragment as HomeFragment2).onNetworkRetry = { loadApis() }
        }

    }
    //endregion

    //region Common
    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> binding.drawerLayout.closeDrawer(GravityCompat.START)
            !mHomeFragment.isVisible -> loadHomeFragment()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RequestCode.ACCOUNT) {
            loadWishListFragment()
        } else if (requestCode == Constants.RequestCode.SETTINGS) {
            SLocaleHelper.setLocale(this, getAppLanguage())
            if (selectedDashboard != getSharedPrefInstance().getIntValue(Constants.SharedPref.KEY_DASHBOARD, 0)) {
                //recreate()
                launchActivityWithNewTask<DashBoardActivity>()
            } else {
                launchActivityWithNewTask<DashBoardActivity>()
            }
        }
    }

    private fun enable(aImageView: ImageView?) {
        disableAll()
        showCartCount()
        aImageView?.background = getDrawable(R.drawable.bg_circle_primary_light)
        aImageView?.applyColorFilter(color(R.color.colorPrimary))
    }

    private fun disableAll() {
        disable(binding.main.bottomBarInclude.ivHome)
        disable(binding.main.bottomBarInclude.ivWishList)
        disable(binding.main.bottomBarInclude.menuCartInclude.ivCart)
        disable(binding.main.bottomBarInclude.ivProfile)
    }

    private fun disable(aImageView: ImageView?) {
        aImageView?.background = null
        aImageView?.applyColorFilter(color(R.color.textColorSecondary))
    }

    private fun setUpDrawerToggle() {
        val toggle = object : ActionBarDrawerToggle(this, binding.drawerLayout, binding.main.toolbarInclude.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                if (getAppLanguage() == "ar") {
                    binding.main.root.translationX = -slideOffset * drawerView.width
                } else {
                    binding.main.root.translationX = slideOffset * drawerView.width
                }
                (binding.drawerLayout).bringChildToFront(drawerView)
                (binding.drawerLayout).requestLayout()
            }
        }
        toggle.setToolbarNavigationClickListener {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(
                ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_drawer,
                        theme
                )
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(binding.llLeftDrawer)) runDelayed(50) {
            binding.drawerLayout.closeDrawer(
                    binding.llLeftDrawer
            )
        }
    }

    fun changeProfile() {
        if (isLoggedIn()) {
            binding.sidebarInclude.civProfile.loadImageFromUrl(getUserProfile(), aPlaceHolderImage = R.drawable.ic_profile)
        }
    }


    //endregion
}